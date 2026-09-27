package de.fanta.fancyfirework.listners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;

public class VoteListener implements Listener {
    private final FancyFirework plugin = FancyFirework.getPlugin();

    private static final long TWO_HOURS_IN_MILLIS = 1000L * 60 * 60 * 2;
    private final Map<UUID, Map<String, Long>> votes = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVote(VotifierEvent e) {
        Vote vote = e.getVote();
        if (!plugin.getConfig().getBoolean("votereward")) {
            return;
        }
        // The 26.2 player view is backed by CopyOnWriteArrayList; name lookup uses a plain HashMap.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getName().equalsIgnoreCase(vote.getUsername())) continue;
            String service = vote.getServiceName();
            try {
                p.getScheduler().execute(plugin, () -> {
                    if (plugin.isStopping() || !p.isOnline()) return;
                    Map<String, Long> lastVotes = votes.computeIfAbsent(p.getUniqueId(), ignored -> new HashMap<>());
                    Long lastTime = lastVotes.get(service);
                    if (lastTime != null && lastTime + TWO_HOURS_IN_MILLIS > System.currentTimeMillis()) return;
                    lastVotes.put(service, System.currentTimeMillis());
                    ItemStack customfirework = plugin.getRegistry().getRandomFireWorkItem();
                    if (p.getInventory().firstEmpty() == -1) {
                        ChatUtil.sendErrorMessage(p, "Your inventory is full " + customfirework.getItemMeta().getDisplayName() + ChatUtil.GREEN + " could not be added to your inventory.");
                    } else {
                        p.getInventory().addItem(customfirework);
                        ChatUtil.sendNormalMessage(p, "You got " + customfirework.getItemMeta().getDisplayName() + ChatUtil.GREEN + " by voting");
                    }
                }, null, 1);
            } catch (IllegalPluginAccessException ignored) {
                // Disable won the race with vote delivery.
            }
            return;
        }
    }
}
