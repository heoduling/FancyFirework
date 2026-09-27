package de.fanta.fancyfirework.listners;

import de.fanta.fancyfirework.FancyFirework;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Level;

public record EventRegistration(FancyFirework plugin) {

    public static final PluginManager pM = Bukkit.getPluginManager();

    public void registerEvents() {
        if (plugin.getServer().getPluginManager().getPlugin("Votifier") != null) {
            if (plugin.getServer().getPluginManager().getPlugin("TreasureChest") == null) {
                pM.registerEvents(new VoteListener(), plugin);
                plugin.getLogger().log(Level.INFO, "已注册投票奖励监听器");
            } else {
                pM.registerEvents(new CubesideVoteListener(), plugin);
                plugin.getLogger().log(Level.INFO, "已注册 Cubeside 投票奖励监听器");
            }
        }
        if (plugin.getServer().getPluginManager().isPluginEnabled("ChestShop")) {
            pM.registerEvents(new ChestShopListener(plugin), plugin);
        }
        pM.registerEvents(new FireworkListener(), plugin);
        pM.registerEvents(new LootTableListener(), plugin);
    }
}
