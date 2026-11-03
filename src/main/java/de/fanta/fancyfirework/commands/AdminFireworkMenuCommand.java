package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.fireworks.AbstractFireWork;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.PlayerSubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdminFireworkMenuCommand extends PlayerSubCommand implements Listener {

    private final FancyFirework plugin;
    private final Set<MenuHolder> activeMenus = ConcurrentHashMap.newKeySet();
    private Class<?> plugManApi;

    public AdminFireworkMenuCommand(FancyFirework plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerPlugManHook();
    }

    @Override
    public boolean onCommand(Player player, Command command, String alias, String commandString, ArgsParser args) {
        List<NamespacedKey> keys = plugin.getRegistry().getKeys().stream()
                .sorted(Comparator.comparing(NamespacedKey::toString)).toList();
        if (keys.size() > 54) {
            ChatUtil.sendErrorMessage(player, "烟花类型超过 54 种，无法打开列表。");
            return true;
        }
        MenuHolder holder = new MenuHolder(keys);
        for (int slot = 0; slot < keys.size(); slot++) {
            ItemStack preview = plugin.getRegistry().get(keys.get(slot)).getItemStack();
            preview.setAmount(1);
            ItemMeta meta = preview.getItemMeta();
            meta.getPersistentDataContainer().remove(AbstractFireWork.FIREWORK_ID);
            List<String> lore = new ArrayList<>(meta.getLore() == null ? List.of() : meta.getLore());
            lore.add("§7左键领取 1 个");
            lore.add("§7中键或 Shift+左键领取一组");
            meta.setLore(lore);
            preview.setItemMeta(meta);
            holder.inventory.setItem(slot, preview);
        }
        activeMenus.add(holder);
        player.openInventory(holder.inventory);
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return FancyFirework.ADMIN_PERMISSION;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof MenuHolder holder)) return;
        boolean alreadyCancelled = event.isCancelled();
        event.setCancelled(true);
        if (alreadyCancelled || plugin.isStopping() || !plugin.isEnabled()
                || !(event.getWhoClicked() instanceof Player player)
                || !player.hasPermission(FancyFirework.ADMIN_PERMISSION)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= holder.keys.size()) return;
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.MIDDLE
                && event.getClick() != ClickType.SHIFT_LEFT) return;

        AbstractFireWork firework = plugin.getRegistry().get(holder.keys.get(slot));
        if (firework == null) return;
        ItemStack gift = firework.getItemStack();
        gift.setAmount(event.getClick() == ClickType.LEFT ? 1 : gift.getMaxStackSize());
        int capacity = 0;
        for (ItemStack existing : player.getInventory().getStorageContents()) {
            if (existing == null || existing.getType().isAir()) capacity += gift.getMaxStackSize();
            else if (existing.isSimilar(gift)) capacity += existing.getMaxStackSize() - existing.getAmount();
        }
        if (capacity < gift.getAmount()) {
            ChatUtil.sendErrorMessage(player, "背包空间不足，无法领取。");
            return;
        }
        player.getInventory().addItem(gift);
        ChatUtil.sendNormalMessage(player, "已领取 " + gift.getAmount() + " 个 " + gift.getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof MenuHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof MenuHolder holder) activeMenus.remove(holder);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder(false) instanceof MenuHolder holder) activeMenus.remove(holder);
    }

    private void registerPlugManHook() {
        Plugin plugMan = Bukkit.getPluginManager().getPlugin("PlugManX");
        if (plugMan == null || !plugMan.isEnabled()) return;
        try {
            ClassLoader loader = plugMan.getClass().getClassLoader();
            Class<?> api = Class.forName("bukkit.com.rylinaux.plugman.api.PlugManAPI", true, loader);
            Class<?> gentle = Class.forName("bukkit.com.rylinaux.plugman.api.GentleUnload", true, loader);
            Object callback = Proxy.newProxyInstance(loader, new Class<?>[]{gentle}, (proxy, method, args) -> {
                if (method.getName().equals("askingForGentleUnload")) {
                    if (!activeMenus.isEmpty()) {
                        plugin.getLogger().warning("请先关闭所有管理员烟花 GUI，再重试 PlugManX 卸载或重载。");
                        return false;
                    }
                    return true;
                }
                if (method.getName().equals("toString")) return "FancyFirework menu unload guard";
                if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                if (method.getName().equals("equals")) return proxy == args[0];
                return null;
            });
            Method register = api.getMethod("pleaseAddMeToGentleUnload", Plugin.class, gentle);
            if ((boolean) register.invoke(null, plugin, callback)) plugManApi = api;
            else plugin.getLogger().warning("PlugManX 卸载保护注册失败，打开 GUI 时请勿热卸载插件。");
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("当前 PlugManX 不支持管理员 GUI 卸载保护：" + e.getMessage());
        }
    }

    public void shutdown() {
        if (plugManApi == null) return;
        try {
            Field field = plugManApi.getDeclaredField("gentleUnloads");
            field.setAccessible(true);
            Map<?, ?> hooks = (Map<?, ?>) field.get(null);
            hooks.remove(plugin);
            plugManApi = null;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("清理 PlugManX 卸载保护失败：" + e.getMessage());
        }
    }

    private static final class MenuHolder implements InventoryHolder {
        private final List<NamespacedKey> keys;
        private final Inventory inventory;

        private MenuHolder(List<NamespacedKey> keys) {
            this.keys = keys;
            this.inventory = Bukkit.createInventory(this, Math.max(9, ((keys.size() + 8) / 9) * 9), "烟花大全 · 管理员");
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
