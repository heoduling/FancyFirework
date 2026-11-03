package ffprobe;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

/** Real-core integration checks with a synthetic player/view; no network client is claimed. */
public final class HolderProbe extends JavaPlugin {
    private Object listener;
    private Method begin, end;
    private int passed, failed;
    private final UUID playerId = UUID.fromString("dd4aba17-0576-48d2-b81f-a4900ab5f773");

    @Override public void onEnable() {
        getCommand("holderprobe").setExecutor((sender, command, alias, args) -> {
            World world = Bukkit.getWorlds().getFirst();
            world.getChunkAtAsync(0, 0, true).thenAccept(chunk ->
                    Bukkit.getRegionScheduler().run(this, new Location(world, 8, 80, 8), task -> {
                        try { runChecks(world); }
                        catch (Throwable error) { getLogger().log(java.util.logging.Level.SEVERE, "HOLDER_FATAL", error); }
                    }));
            return true;
        });
    }

    private void runChecks(World world) throws Exception {
        passed = failed = 0;
        Class<?> agent = Class.forName("ffprobe.SnapshotCounter", true, null);
        begin = agent.getMethod("begin"); end = agent.getMethod("end");
        listener = java.util.Arrays.stream(InventoryClickEvent.getHandlerList().getRegisteredListeners())
                .filter(registration -> registration.getPlugin().getName().equals("FancyFirework"))
                .map(registration -> registration.getListener())
                .filter(value -> value.getClass().getSimpleName().equals("AdminFireworkMenuCommand"))
                .findFirst().orElseThrow();
        var block = world.getBlockAt(8, 80, 8);
        block.setType(Material.CHEST);
        Inventory chest = ((Chest) block.getState(false)).getBlockInventory();
        chest.clear();
        ItemStack paper = new ItemStack(Material.PAPER);
        var meta = paper.getItemMeta();
        Component text = Component.text("holder probe");
        for (int i = 0; i < 12; i++) text = Component.text("nested-" + i).append(text);
        meta.lore(java.util.Collections.nCopies(24, text));
        paper.setItemMeta(meta);
        for (int slot = 0; slot < chest.getSize(); slot++) chest.setItem(slot, paper.clone());
        check("agent_active", "true".equals(System.getProperty("ffprobe.snapshot.instrumented")));
        check("control_snapshot_true", snapshots(() -> chest.getHolder()) == 1);
        check("control_snapshot_false", snapshots(() -> chest.getHolder(false)) == 0);

        FakePlayer ordinary = new FakePlayer(world, false);
        ordinary.top = chest;
        noSnapshot("ordinary_click", () -> invoke("onClick", ordinary.click(0, ClickType.LEFT, false)));
        noSnapshot("ordinary_drag", () -> invoke("onDrag", ordinary.drag()));
        noSnapshot("ordinary_close", () -> invoke("onClose", new InventoryCloseEvent(ordinary.view)));
        noSnapshot("ordinary_quit", () -> invoke("onQuit", new PlayerQuitEvent(ordinary.player, (Component) null)));

        for (ClickType type : List.of(ClickType.LEFT, ClickType.MIDDLE, ClickType.SHIFT_LEFT)) {
            FakePlayer admin = openMenu(world);
            InventoryClickEvent event = admin.click(admin.fireworkSlot, type, false);
            long count = snapshots(() -> invoke("onClick", event));
            int expected = type == ClickType.LEFT ? 1 : 64;
            getLogger().info("HOLDER_GRANT click=" + type + " amount=" + admin.itemCount());
            check("menu_" + type + "_grant", admin.itemCount() == expected && event.isCancelled() && count == 0);
            invoke("onClose", new InventoryCloseEvent(admin.view));
        }

        FakePlayer menu = openMenu(world);
        var drag = menu.drag(); invoke("onDrag", drag);
        check("menu_drag_blocked", drag.isCancelled());
        var right = menu.click(0, ClickType.RIGHT, false); invoke("onClick", right);
        check("menu_right_no_grant", right.isCancelled() && menu.itemCount() == 0);
        var cancelled = menu.click(0, ClickType.LEFT, true); invoke("onClick", cancelled);
        check("cancelled_event_no_grant", menu.itemCount() == 0);
        var bottom = menu.click(menu.top.getSize(), ClickType.SHIFT_LEFT, false); invoke("onClick", bottom);
        check("bottom_shift_blocked", bottom.isCancelled() && menu.itemCount() == 0);
        var doubleClick = menu.click(menu.top.getSize(), ClickType.DOUBLE_CLICK, false); invoke("onClick", doubleClick);
        check("bottom_double_click_blocked", doubleClick.isCancelled() && menu.itemCount() == 0);
        menu.permission = false;
        var denied = menu.click(0, ClickType.LEFT, false); invoke("onClick", denied);
        check("permission_denied_no_grant", denied.isCancelled() && menu.itemCount() == 0);
        menu.permission = true;
        for (int slot = 0; slot < 36; slot++) menu.backpack.setItem(slot, new ItemStack(Material.STONE, 64));
        invoke("onClick", menu.click(0, ClickType.LEFT, false));
        check("full_inventory_no_grant", menu.itemCount() == 2304 && menu.backpack.getItem(0).getType() == Material.STONE);
        check("preview_has_no_firework_id", !menu.top.getItem(0).getItemMeta().getPersistentDataContainer()
                .getKeys().contains(org.bukkit.NamespacedKey.fromString("fancyfirework:firework")));
        invoke("onClose", new InventoryCloseEvent(menu.view));
        check("close_cleans_menu", activeMenus().isEmpty());
        FakePlayer quitting = openMenu(world);
        invoke("onQuit", new PlayerQuitEvent(quitting.player, (Component) null));
        check("quit_cleans_menu", activeMenus().isEmpty());
        getLogger().info("HOLDER_RESULT passed=" + passed + " failed=" + failed);
    }

    private void noSnapshot(String name, Runnable operation) throws Exception {
        long count = snapshots(operation);
        getLogger().info("HOLDER_SNAPSHOTS case=" + name + " count=" + count);
        check(name + "_no_snapshot", count == 0);
    }

    private long snapshots(Runnable operation) throws Exception {
        begin.invoke(null);
        long count;
        try { operation.run(); } finally { count = ((Number) end.invoke(null)).longValue(); }
        return count;
    }

    private void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        getLogger().info("HOLDER_CHECK " + name + " " + (ok ? "PASS" : "FAIL"));
    }

    private void invoke(String name, Object event) {
        try { listener.getClass().getMethod(name, event.getClass()).invoke(listener, event); }
        catch (ReflectiveOperationException error) { throw new RuntimeException(name, error); }
    }

    private Set<?> activeMenus() throws Exception {
        var field = listener.getClass().getDeclaredField("activeMenus");
        field.setAccessible(true); return (Set<?>) field.get(listener);
    }

    private FakePlayer openMenu(World world) throws Exception {
        FakePlayer player = new FakePlayer(world, true);
        Method command = java.util.Arrays.stream(listener.getClass().getMethods())
                .filter(method -> method.getName().equals("onCommand") && method.getParameterCount() == 5
                        && method.getParameterTypes()[0] == Player.class).findFirst().orElseThrow();
        command.invoke(listener, player.player, null, "ff", "menu", null);
        check("menu_opened", player.top != null && player.top.getSize() >= 9);
        Object holder = player.top.getHolder(false);
        var keysField = holder.getClass().getDeclaredField("keys");
        keysField.setAccessible(true);
        List<?> keys = (List<?>) keysField.get(holder);
        for (int slot = 0; slot < keys.size(); slot++) {
            if (keys.get(slot).toString().equals("fancyfirework:battery_blue")) player.fireworkSlot = slot;
        }
        if (player.fireworkSlot < 0 || player.top.getItem(player.fireworkSlot).getType() != Material.PLAYER_HEAD) {
            throw new IllegalStateException("Missing 64-stack blue battery fixture");
        }
        return player;
    }

    private final class FakePlayer {
        final Inventory backpack = Bukkit.createInventory(null, 36);
        final PlayerInventory inventory;
        final Player player;
        InventoryView view;
        Inventory top;
        int fireworkSlot = -1;
        boolean permission;

        FakePlayer(World world, boolean permission) {
            this.permission = permission;
            inventory = (PlayerInventory) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{PlayerInventory.class},
                    (proxy, method, args) -> {
                        try { return Inventory.class.getMethod(method.getName(), method.getParameterTypes()).invoke(backpack, args); }
                        catch (NoSuchMethodException ignored) { return defaultValue(method.getReturnType()); }
                    });
            player = (Player) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{Player.class}, (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "getInventory" -> inventory;
                    case "getName", "getDisplayName", "getPlayerListName" -> "HolderProbe";
                    case "getUniqueId" -> playerId;
                    case "hasPermission", "isOp", "isPermissionSet" -> this.permission;
                    case "getServer" -> Bukkit.getServer();
                    case "getWorld" -> world;
                    case "getLocation" -> new Location(world, 8, 81, 8);
                    case "isOnline", "isValid" -> true;
                    case "getOpenInventory" -> this.view;
                    case "openInventory" -> { top = (Inventory) args[0]; yield this.view; }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "SyntheticHolderProbePlayer";
                    default -> defaultValue(method.getReturnType());
                };
            });
            view = (InventoryView) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{InventoryView.class}, (proxy, method, args) -> {
                int slot = args != null && args.length > 0 && args[0] instanceof Integer number ? number : 0;
                return switch (method.getName()) {
                    case "getTopInventory" -> top;
                    case "getBottomInventory" -> inventory;
                    case "getPlayer" -> player;
                    case "getType" -> top.getType();
                    case "getInventory" -> slot < top.getSize() ? top : inventory;
                    case "convertSlot" -> slot < top.getSize() ? slot : slot - top.getSize();
                    case "getItem" -> slot < top.getSize() ? top.getItem(slot) : backpack.getItem(slot - top.getSize());
                    default -> defaultValue(method.getReturnType());
                };
            });
        }

        InventoryClickEvent click(int slot, ClickType type, boolean cancelled) {
            InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, type,
                    type == ClickType.DOUBLE_CLICK ? InventoryAction.COLLECT_TO_CURSOR : InventoryAction.PICKUP_ALL);
            event.setCancelled(cancelled); return event;
        }

        InventoryDragEvent drag() {
            return new InventoryDragEvent(view, null, new ItemStack(Material.PAPER), false,
                    Map.of(0, new ItemStack(Material.PAPER)));
        }

        int itemCount() {
            return java.util.Arrays.stream(backpack.getStorageContents()).filter(java.util.Objects::nonNull)
                    .mapToInt(ItemStack::getAmount).sum();
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        return 0;
    }
}
