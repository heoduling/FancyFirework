package de.fanta.fancyfirework.listners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.cubeside.connection.GlobalPlayer;
import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.plugin.CubesideUtilsBukkit;
import de.iani.playerUUIDCache.CachedPlayer;
import de.iani.playerUUIDCache.PlayerUUIDCache;
import de.iani.treasurechest.TreasureChest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CubesideVoteListener implements Listener {
    private final FancyFirework plugin = FancyFirework.getPlugin();

    private record VoteKey(UUID player, String service) { }

    private final Cache<VoteKey, Boolean> votes = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS).build();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVote(VotifierEvent e) {
        PlayerUUIDCache playerUUIDCache = (PlayerUUIDCache) Bukkit.getPluginManager().getPlugin("PlayerUUIDCache");
        TreasureChest treasureChest = (TreasureChest) Bukkit.getPluginManager().getPlugin("TreasureChest");
        Vote vote = e.getVote();

        ItemStack customfirework = plugin.getRegistry().getRandomValentineFireWorkItem();
        if (!plugin.getRuntimeConfig().voteReward()) {
            return;
        }
        if (playerUUIDCache == null) {
            return;
        }
        if (treasureChest == null) {
            return;
        }
        CachedPlayer cp = playerUUIDCache.getPlayerFromNameOrUUID(vote.getUsername().trim());

        if (cp != null && customfirework != null) {
            if (votes.asMap().putIfAbsent(new VoteKey(cp.getUniqueId(), vote.getServiceName()), Boolean.TRUE) != null) return;

            ItemStack displayItem = new ItemStack(Material.FIREWORK_ROCKET, 1);
            ItemMeta displayItemMeta = displayItem.getItemMeta();
            displayItemMeta.setDisplayName(ChatUtil.GREEN + "FancyFirework");
            displayItem.setItemMeta(displayItemMeta);

            treasureChest.addItem(cp.getName(), displayItem, new ItemStack[] { customfirework.clone() }, 0);
            GlobalPlayer globalPlayer = CubesideUtilsBukkit.getInstance().getGlobalDataHelper().getPlayer(cp.getUniqueId());
            if (globalPlayer != null) {
                CubesideUtilsBukkit.getInstance().getGlobalDataHelper().sendActionBarMessage(globalPlayer,
                        Component.text("投票奖励：", NamedTextColor.GREEN).append(customfirework.getItemMeta().displayName()).append(Component.text("，请到宝箱领取。")));
            }
        }
    }
}
