package de.fanta.fancyfirework.listners;

import de.fanta.fancyfirework.FancyFirework;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.IllegalPluginAccessException;

public class LootTableListener implements Listener {
    private final FancyFirework plugin = FancyFirework.getPlugin();
    private final Set<Object> lootTableSaveList = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onPlayerUseLootChest(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        if (chest.getLootTable() == null) {
            return;
        }

        if (plugin.getConfig().getBoolean("loottable.enabled")) {
            addFireworktoLootChest(chest.getInventory(), chest.getLocation(), null);
        }
    }

    @EventHandler
    public void onPlayerUseEntityLootChest(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();

        if (!(entity instanceof StorageMinecart minecart)) {
            return;
        }

        if (plugin.getConfig().getBoolean("loottable.enabled")) {
            try {
                minecart.getScheduler().execute(plugin, () -> {
                    if (!plugin.isStopping() && minecart.isValid() && minecart.getLootTable() != null) {
                        addFireworktoLootChest(minecart.getInventory(), minecart.getUniqueId(), minecart);
                    }
                }, null, 1);
            } catch (IllegalPluginAccessException ignored) {
                // The plugin was disabled before the minecart could be checked.
            }
        }

    }

    private void addFireworktoLootChest(Inventory chest, Object id, StorageMinecart minecart) {
        if (!lootTableSaveList.add(id)) return;
        Random random = new Random();
        double chance = plugin.getConfig().getDouble("loottable.chance", 0.5);
        boolean hasFireWork = random.nextDouble() < chance;
        if (hasFireWork) {
            ItemStack randomFirework = plugin.getRegistry().getRandomFireWorkItem();
            Runnable fill = () -> {
                if (plugin.isStopping()) return;
                List<Integer> freeSlots = new ArrayList<>();
                for (int i = 0; i < chest.getSize(); i++) {
                    if (chest.getItem(i) == null) {
                        freeSlots.add(i);
                    }
                }
                if (!freeSlots.isEmpty()) {
                    chest.setItem(freeSlots.get(random.nextInt(freeSlots.size())), randomFirework);
                }
            };
            try {
                if (minecart == null) {
                    plugin.getScheduler().runLocalDelayed(chest.getLocation(), fill, 1L);
                } else {
                    minecart.getScheduler().runDelayed(plugin, task -> fill.run(), null, 1L);
                }
            } catch (IllegalPluginAccessException ignored) {
                // Plugin is stopping; the delayed inventory write is discarded.
            }
        }
    }
}
