package de.fanta.fancyfirework.fireworks.defaults;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ColorUtils;
import de.fanta.fancyfirework.utils.CustomFireworkHeads;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class FireWorkFountainRainbow extends FireWorkFountain {

    private static final FancyFirework plugin = FancyFirework.getPlugin();

    public FireWorkFountainRainbow() {
        super(new NamespacedKey(FancyFirework.getPlugin(), "fountain_rainbow"));
    }

    @Override
    protected ItemStack createItemStack() {
        ItemStack itemStack = CustomFireworkHeads.getCustomTextureHead(UUID.fromString("0dc8eb51-cc56-418d-886b-f0056847db64"), "Rainbow Cube",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg3ZmQyM2E3ODM2OWJkMzg3NWRhODg5NmYxNTBjNGFmOWYyMzM3NGUwNDhlMzA5MTM5MDBlM2ZkZDc3ODU5YSJ9fX0=");
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.of("#f26970") + "彩" + ChatColor.of("#ffb35c") + "虹" + ChatColor.of("#f7e364") + "喷" + ChatColor.of("#71fe94") + "泉" + ChatColor.of("#75a3ff") + "烟" + ChatColor.of("#cd70ff") + "花");
        meta.setLore(FancyFirework.getPlugin().getConfig().getStringList("itemlore"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public Color randomColor() {
        return ColorUtils.getColor(plugin.getTime(), 0.1);
    }

}



