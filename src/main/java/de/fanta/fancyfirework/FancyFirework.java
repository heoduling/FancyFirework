package de.fanta.fancyfirework;

import de.fanta.fancyfirework.commands.CommandRegistration;
import de.fanta.fancyfirework.fireworks.FireWorkRegistration;
import de.fanta.fancyfirework.listners.AFKListener;
import de.fanta.fancyfirework.listners.EventRegistration;
import de.fanta.fancyfirework.schedular.GlobalScheduler;
import de.fanta.fancyfirework.schedular.Scheduler;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.fanta.fancyfirework.utils.WorldGuardHelper;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class FancyFirework extends JavaPlugin {

    public static final String PREFIX = ChatUtil.BLUE + "[" + ChatUtil.GREEN + "FancyFirework" + ChatUtil.BLUE + "]";
    public static final String ADMIN_PERMISSION = "fancyfirework.admin";
    public static final String MOD_PERMISSION = "fancyfirework.mod";
    public static Logger LOGGER;
    private static FancyFirework plugin;
    private FireWorkWorks fireWorkWorks;
    private FireWorksRegistry registry;
    private VanishPlugin vanishNoPacketPlugin;
    private boolean isSuperVanishPluginInstalled = false;
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

        if (plugin.getServer().getPluginManager().getPlugin("VanishNoPacket") != null) {
            vanishNoPacketPlugin = (VanishPlugin) plugin.getServer().getPluginManager().getPlugin("VanishNoPacket");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            isSuperVanishPluginInstalled = true;
        }

        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHelper = new WorldGuardHelper(getServer().getPluginManager().getPlugin("WorldGuard"));
        }

        scheduler = new GlobalScheduler(this);
        new bStats(this).registerbStats();

        fireWorkWorks = new FireWorkWorks();
        this.registry = new FireWorksRegistry(this);

        new CommandRegistration(this).registerCommands();
        new EventRegistration(this).registerEvents();
        new FireWorkRegistration(this).registerFirework();

        loadConfig();
        saveConfig();

        this.time = 0;
        this.startColorTimeTask(1);

        getScheduler().runGlobalAtFixedRate(AFKListener::onTimer, 2 * 20, 10);

        for (Player p : getServer().getOnlinePlayers()) {
            AFKListener.handleJoin(p);
        }
    }

    @Override
    public void onDisable() {
        stopping = true;
        if (fireWorkWorks != null) fireWorkWorks.shutdown();
        if (registry != null) registry.cancelAllTasks();
        if (AFKListener.playerTimes != null) AFKListener.playerTimes.clear();
        PluginManager pm = getServer().getPluginManager();
        for (Permission permission : ownedPermissions) {
            if (pm.getPermission(permission.getName()) == permission) pm.removePermission(permission);
        }
        ownedPermissions.clear();
    }

    public FireWorkWorks getFireWorkWorks() {
        return fireWorkWorks;
    }

    public FireWorksRegistry getRegistry() {
        return registry;
    }

    public boolean isVanish(Player p) {
        if (vanishNoPacketPlugin != null) {
            return vanishNoPacketPlugin.getManager().isVanished(p);
        } else if (isSuperVanishPluginInstalled) {
            return VanishAPI.isInvisible(p);
        } else {
            return false;
        }
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
        if (fireWorkWorks != null) fireWorkWorks.reloadSpawnRate();
    }

    public void loadConfig() {
        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true);
        redstonemode = cfg.getBoolean("redstonemode", false);
        dispenserMode = cfg.getBoolean("dispenserMode", true);
        dispenserDelaSeconds = cfg.getInt("dispenserDelaSeconds", 30);

        //AFK
        AFKListener.maxIdleTime = Math.max(plugin.getConfig().getInt("maxAFKTime"), 2) * 1000;
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
