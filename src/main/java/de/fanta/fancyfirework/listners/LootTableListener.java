package de.fanta.fancyfirework.listners;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.FancyFirework.RuntimeConfig;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.plugin.IllegalPluginAccessException;

public class LootTableListener implements Listener {
    private final FancyFirework plugin = FancyFirework.getPlugin();
    private final NamespacedKey checkedKey = new NamespacedKey(plugin, "loot_checked");

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

        RuntimeConfig cfg = plugin.getRuntimeConfig();
        if (cfg.lootTableEnabled() && markChecked(chest.getPersistentDataContainer()) && chest.update()) {
            addFireworktoLootChest(chest.getInventory(), null, cfg);
        }
    }

    @EventHandler
    public void onPlayerUseEntityLootChest(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();

        if (!(entity instanceof StorageMinecart minecart)) {
            return;
        }

        RuntimeConfig cfg = plugin.getRuntimeConfig();
        if (cfg.lootTableEnabled()) {
            try {
                minecart.getScheduler().execute(plugin, () -> {
                    if (!plugin.isStopping() && minecart.isValid() && minecart.getLootTable() != null
                            && markChecked(minecart.getPersistentDataContainer())) {
                        addFireworktoLootChest(minecart.getInventory(), minecart, cfg);
                    }
                }, null, 1);
            } catch (IllegalPluginAccessException ignored) {
                // The plugin was disabled before the minecart could be checked.
            }
        }

    }

    private boolean markChecked(PersistentDataContainer data) {
        if (data.has(checkedKey, PersistentDataType.BYTE)) return false;
        data.set(checkedKey, PersistentDataType.BYTE, (byte) 1);
        return true;
    }

    private void addFireworktoLootChest(Inventory chest, StorageMinecart minecart, RuntimeConfig cfg) {
        Random random = new Random();
        double chance = cfg.lootTableChance();
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
