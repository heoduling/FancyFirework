package de.fanta.fancyfirework.fireworks.defaults;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.fireworks.ItemFireWork;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SpookyBomb extends ItemFireWork {

    private static final FancyFirework plugin = FancyFirework.getPlugin();
    private final Set<UUID> spookyPlayers = ConcurrentHashMap.newKeySet();

    public SpookyBomb() {
        super(new NamespacedKey(FancyFirework.getPlugin(), "spooky_bomb"));
    }

    @Override
    protected ItemStack createItemStack() {
        ItemStack stack = new ItemStack(Material.EGG, 16);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("" + ChatColor.of("#16705A") + ChatColor.BOLD + "惊魂弹");
            meta.setLore(FancyFirework.getPlugin().getConfig().getStringList("itemlorebangsnap"));
            meta.setCustomModelData(354461);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override
    public void onLaunch(Player player, Projectile entity) {

    }

    @Override
    public void onHit(Player player, ProjectileHitEvent event) {
        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();

        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 2f, 1f);
        world.spawnParticle(Particle.DUST, loc, 80, 1, 1.5, 1, 0.008, new Particle.DustOptions(Color.fromRGB(22, 112, 90), 2.5f), true);
        world.spawnParticle(Particle.DUST, loc, 80, 1, 1.5, 1, 0.008, new Particle.DustOptions(Color.fromRGB(17, 51, 43), 2.5f), true);
        world.spawnParticle(Particle.DUST, loc, 80, 1, 1.5, 1, 0.008, new Particle.DustOptions(Color.fromRGB(69, 69, 69), 2.5f), true);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 70, 0.5, 1.5, 0.5, 0.005, null, true);

        for (Entity entity : world.getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player spookyPlayer) {
                UUID id = spookyPlayer.getUniqueId();
                if (spookyPlayers.add(id)) {
                    AtomicInteger tick = new AtomicInteger();
                    try {
                        var task = spookyPlayer.getScheduler().runAtFixedRate(plugin, scheduled -> {
                            if (plugin.isStopping() || !spookyPlayer.isOnline()) {
                                spookyPlayers.remove(id);
                                scheduled.cancel();
                                return;
                            }
                            int current = tick.getAndIncrement();
                            if (current == 0) {
                                spookyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 85, 0, true));
                                spookyPlayer.playSound(spookyPlayer, Sound.ENTITY_GHAST_HURT, SoundCategory.AMBIENT, 2, 1);
                            }
                            if (current < 4) {
                                spookyPlayer.playSound(spookyPlayer, Sound.ENTITY_WARDEN_HEARTBEAT, SoundCategory.AMBIENT, 1, 1);
                            } else {
                                spookyPlayers.remove(id);
                                scheduled.cancel();
                            }
                        }, () -> spookyPlayers.remove(id), 1, 16);
                        if (task == null) spookyPlayers.remove(id);
                    } catch (IllegalPluginAccessException ignored) {
                        spookyPlayers.remove(id);
                    }
                }
            }
        }
    }
}
