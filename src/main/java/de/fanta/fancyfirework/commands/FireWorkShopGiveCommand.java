package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.fireworks.AbstractFireWork;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.ArrayList;
import java.util.List;

/** Gives real FancyFirework items for command-based shops without granting players /ff give. */
public class FireWorkShopGiveCommand extends SubCommand {

    private static final int MAX_AMOUNT = 2304;
    private final FancyFirework plugin;

    public FireWorkShopGiveCommand(FancyFirework plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            ChatUtil.sendErrorMessage(sender, "此命令只能由服务器控制台执行。");
            return true;
        }
        if (!args.hasNext()) return usage(sender);
        String playerName = args.getNext();
        if (!args.hasNext()) return usage(sender);
        String keyText = args.getNext();
        if (!args.hasNext()) return usage(sender);
        String amountText = args.getNext();
        if (args.hasNext()) return usage(sender);

        int amount;
        try {
            amount = Integer.parseInt(amountText);
        } catch (NumberFormatException ignored) {
            ChatUtil.sendErrorMessage(sender, "数量必须是 1 到 " + MAX_AMOUNT + " 的整数。");
            return true;
        }
        if (amount < 1 || amount > MAX_AMOUNT) {
            ChatUtil.sendErrorMessage(sender, "数量必须是 1 到 " + MAX_AMOUNT + " 的整数。");
            return true;
        }

        NamespacedKey key;
        try {
            key = NamespacedKey.fromString(keyText);
        } catch (IllegalArgumentException ignored) {
            key = null;
        }
        AbstractFireWork firework = key == null ? null : plugin.getRegistry().get(key);
        if (firework == null) {
            ChatUtil.sendErrorMessage(sender, "烟花 " + keyText + " 不存在。");
            return true;
        }

        Player target = null;
        // On the target 26.2 core the online-player view is backed by CopyOnWriteArrayList.
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().equalsIgnoreCase(playerName)) {
                target = online;
                break;
            }
        }
        if (target == null) {
            ChatUtil.sendErrorMessage(sender, "玩家 " + playerName + " 不在线，烟花未发放。");
            return true;
        }

        Player recipient = target;
        String delivery = "玩家 " + playerName + "，烟花 " + firework.getKey() + " x" + amount;
        try {
            boolean accepted = recipient.getScheduler().execute(plugin,
                    () -> give(recipient, firework, amount),
                    () -> plugin.getLogger().warning("商店烟花未发放：" + delivery + "，玩家已离线。"), 1);
            if (!accepted) plugin.getLogger().warning("商店烟花未发放：" + delivery + "，玩家调度未接受任务。");
        } catch (IllegalPluginAccessException ignored) {
            plugin.getLogger().warning("商店烟花未发放：" + delivery + "，FancyFirework 正在停用。");
        }
        return true;
    }

    private void give(Player target, AbstractFireWork firework, int amount) {
        if (plugin.isStopping() || !plugin.isEnabled() || !target.isOnline()) {
            plugin.getLogger().warning("商店烟花未发放：玩家 " + target.getName() + "，烟花 " + firework.getKey()
                    + " x" + amount + "，玩家已离线或插件正在停用。");
            return;
        }

        Inventory inventory = target.getInventory();
        ItemStack template = firework.getItemStack();
        template.setAmount(1);
        int stackSize = Math.min(template.getMaxStackSize(), inventory.getMaxStackSize());
        int capacity = 0;
        for (ItemStack existing : inventory.getStorageContents()) {
            if (existing == null || existing.getType().isAir()) capacity += stackSize;
            else if (existing.isSimilar(template)) {
                capacity += Math.max(0, Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize()) - existing.getAmount());
            }
        }
        if (capacity < amount) {
            ChatUtil.sendErrorMessage(target, "背包空间不足，商店烟花未发放，请联系管理员处理此次购买。");
            plugin.getLogger().warning("商店烟花未发放：玩家 " + target.getName() + " 背包空间不足，烟花 " + firework.getKey() + " x" + amount);
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = amount; remaining > 0; remaining -= stackSize) {
            ItemStack stack = template.clone();
            stack.setAmount(Math.min(remaining, stackSize));
            stacks.add(stack);
        }
        var leftovers = inventory.addItem(stacks.toArray(ItemStack[]::new));
        if (!leftovers.isEmpty()) {
            plugin.getLogger().severe("商店烟花发放不完整：玩家 " + target.getName() + "，烟花 " + firework.getKey()
                    + " x" + amount + "，剩余 " + leftovers.values().stream().mapToInt(ItemStack::getAmount).sum());
            ChatUtil.sendErrorMessage(target, "商店烟花发放不完整，请联系管理员处理此次购买。");
            return;
        }
        ChatUtil.sendNormalMessage(target, "已收到商店烟花 " + firework.getKey() + " x" + amount);
        plugin.getLogger().info("商店烟花已发放：玩家 " + target.getName() + "，烟花 " + firework.getKey() + " x" + amount);
    }

    private boolean usage(CommandSender sender) {
        ChatUtil.sendWarningMessage(sender, "用法：/ff shopgive <玩家名> <烟花键> <数量>");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return FancyFirework.ADMIN_PERMISSION;
    }
}
