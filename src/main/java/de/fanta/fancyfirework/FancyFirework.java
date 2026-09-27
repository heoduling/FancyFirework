package de.fanta.fancyfirework;

import de.fanta.fancyfirework.commands.CommandRegistration;
import de.fanta.fancyfirework.commands.AdminFireworkMenuCommand;
import de.fanta.fancyfirework.fireworks.FireWorkRegistration;
import de.fanta.fancyfirework.listners.EventRegistration;
import de.fanta.fancyfirework.schedular.GlobalScheduler;
import de.fanta.fancyfirework.schedular.Scheduler;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.fanta.fancyfirework.utils.WorldGuardHelper;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class FancyFirework extends JavaPlugin {

    public static final String PREFIX = ChatUtil.BLUE + "[" + ChatUtil.GREEN + "FancyFirework" + ChatUtil.BLUE + "]";
    public static final String ADMIN_PERMISSION = "fancyfirework.admin";
    public static final String MOD_PERMISSION = "fancyfirework.mod";
    public static Logger LOGGER;
    private static FancyFirework plugin;
    private FireWorksRegistry registry;
    private AdminFireworkMenuCommand adminMenu;
    private WorldGuardHelper worldGuardHelper;
    private volatile long time;
    private volatile boolean stopping;
    private final List<Permission> ownedPermissions = new ArrayList<>();
    private boolean redstonemode;
    private boolean dispenserMode;
    private int dispenserDelaSeconds;

    private boolean disableFireWorkUse = false;

    private boolean HAS_PLAYER_PROFILE_API;
    private Scheduler scheduler;

    public static FancyFirework getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        stopping = false;
        LOGGER = getLogger();
        plugin = this;

        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHelper = new WorldGuardHelper(getServer().getPluginManager().getPlugin("WorldGuard"));
        }

        scheduler = new GlobalScheduler(this);
        new bStats(this).registerbStats();

        loadConfig();
        saveConfig();

        this.registry = new FireWorksRegistry();

        adminMenu = new AdminFireworkMenuCommand(this);
        new CommandRegistration(this, adminMenu).registerCommands();
        new EventRegistration(this).registerEvents();
        new FireWorkRegistration(this).registerFirework();

        this.time = 0;
        this.startColorTimeTask(1);
    }

    @Override
    public void onDisable() {
        stopping = true;
        if (adminMenu != null) adminMenu.shutdown();
        if (registry != null) registry.cancelAllTasks();
        PluginManager pm = getServer().getPluginManager();
        for (Permission permission : ownedPermissions) {
            if (pm.getPermission(permission.getName()) == permission) pm.removePermission(permission);
        }
        ownedPermissions.clear();
    }

    public FireWorksRegistry getRegistry() {
        return registry;
    }

    public void startColorTimeTask(long l) {
        getScheduler().runGlobalAtFixedRate(() -> FancyFirework.this.time++, 1L, l);
    }

    public long getTime() {
        return this.time;
    }

    public boolean canBuild(Player player, Location loc) {
        if (worldGuardHelper == null) {
            return true;
        } else {
            return worldGuardHelper.canBuild(player, loc);
        }
    }

    public void reloadFireworkConfig() {
        reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true);
        if (cfg.getStringList("itemlore").equals(List.of("§aThe fireworks can be placed", "§aand lit with a flint and steel"))) {
            cfg.set("itemlore", List.of("§a可将烟花放置在地上", "§a使用打火石点燃"));
        }
        if (cfg.getStringList("itemlorebangsnap").equals(List.of("§aThrow them on the floor for fun"))) {
            cfg.set("itemlorebangsnap", List.of("§a扔到地上即可触发"));
        }
        redstonemode = cfg.getBoolean("redstonemode", false);
        dispenserMode = cfg.getBoolean("dispenserMode", true);
        dispenserDelaSeconds = cfg.getInt("dispenserDelaSeconds", 30);
    }

    public boolean isRedstonemode() {
        return redstonemode;
    }

    public boolean isDispenserMode() {
        return dispenserMode;
    }

    public int getDispenserDelaSeconds() {
        return dispenserDelaSeconds;
    }

    public boolean isDisableFireWorkUse() {
        return disableFireWorkUse;
    }

    public void setDisableFireWorkUse(boolean disableFireWorkUse) {
        this.disableFireWorkUse = disableFireWorkUse;
    }

    public void registerPermission(Permission permission) {
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPermission(permission.getName()) == null) {
            pm.addPermission(permission);
            ownedPermissions.add(permission);
        }
    }

    public boolean isStopping() {
        return stopping;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
