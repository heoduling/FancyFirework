package de.fanta.fancyfirework.listners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;

public class VoteListener implements Listener {
    private final FancyFirework plugin = FancyFirework.getPlugin();

    private record VoteKey(UUID player, String service) { }

    private final Cache<VoteKey, Boolean> votes = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS).build();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVote(VotifierEvent e) {
        Vote vote = e.getVote();
        if (!plugin.getRuntimeConfig().voteReward()) {
            return;
        }
        // The 26.2 player view is backed by CopyOnWriteArrayList; name lookup uses a plain HashMap.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getName().equalsIgnoreCase(vote.getUsername())) continue;
            String service = vote.getServiceName();
            try {
                p.getScheduler().execute(plugin, () -> {
                    if (plugin.isStopping() || !p.isOnline()) return;
                    if (votes.asMap().putIfAbsent(new VoteKey(p.getUniqueId(), service), Boolean.TRUE) != null) return;
                    ItemStack customfirework = plugin.getRegistry().getRandomFireWorkItem();
                    if (p.getInventory().firstEmpty() == -1) {
                        ChatUtil.sendErrorMessage(p, "背包已满，无法领取投票奖励 " + customfirework.getItemMeta().getDisplayName() + "。");
                    } else {
                        p.getInventory().addItem(customfirework);
                        ChatUtil.sendNormalMessage(p, "投票奖励：" + customfirework.getItemMeta().getDisplayName());
                    }
                }, null, 1);
            } catch (IllegalPluginAccessException ignored) {
                // Disable won the race with vote delivery.
            }
            return;
        }
    }
}
