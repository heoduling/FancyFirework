package de.fanta.fancyfirework;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public record bStats(FancyFirework plugin) {

    public Metrics registerbStats() {
        int pluginId = 13728;
        Metrics metrics = new Metrics(plugin, pluginId);
        metrics.addCustomChart(new SimplePie("vote_reward", () -> String.valueOf(plugin.getRuntimeConfig().voteReward())));
        metrics.addCustomChart(new SimplePie("loottable", () -> String.valueOf(plugin.getRuntimeConfig().lootTableEnabled())));
        metrics.addCustomChart(new SimplePie("redstone_mode", () -> String.valueOf(plugin.getRuntimeConfig().redstoneMode())));
        metrics.addCustomChart(new SimplePie("wandering_trader", () -> String.valueOf(plugin.getRuntimeConfig().wanderingTraderEnabled())));
        return metrics;
    }
}


