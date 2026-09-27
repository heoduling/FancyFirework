package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.fireworks.AbstractFireWork;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.PlayerSubCommand;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.ArrayList;

public class FireWorkGiveCommand extends PlayerSubCommand {

    private final FancyFirework plugin;

    public FireWorkGiveCommand(FancyFirework plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(Player player, Command command, String s, String s1, ArgsParser args) {
        Player givePlayer = player;
        if (args.hasNext()) {
            String key = args.getNext();
            AbstractFireWork fireWork = plugin.getRegistry().get(NamespacedKey.fromString(key));
            int amount;
            if (args.hasNext()) {
                int next = args.getNext(1);
                if (next < 1 || next > 64) {
                    ChatUtil.sendErrorMessage(player, "Quantity must be between 1-64.");
                    return true;
                }
                amount = next;

                if (args.hasNext()) {
                    if (!player.hasPermission(FancyFirework.ADMIN_PERMISSION)) {
                        ChatUtil.sendErrorMessage(player, "You do not have the permission to give fireworks to other players.");
                        return true;
                    }
                    String giveName = args.getNext();
                    givePlayer = null;
                    // The 26.2 player view is backed by CopyOnWriteArrayList; name lookup uses a plain HashMap.
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.getName().equalsIgnoreCase(giveName)) {
                            givePlayer = online;
                            break;
                        }
                    }

                    if (givePlayer == null) {
                        ChatUtil.sendErrorMessage(player, "Player " + giveName + " is not online.");
                        return true;
                    }
                }
            } else {
                ChatUtil.sendWarningMessage(player, "/fancyfirework give [key] <amount> <PlayerName>");
                return true;
            }
            if (fireWork != null) {
                if (givePlayer == player) {
                    give(player, player, fireWork, amount);
                } else {
                    Player target = givePlayer;
                    String targetName = target.getName();
                    try {
                        boolean accepted = target.getScheduler().execute(plugin,
                                () -> give(player, target, fireWork, amount),
                                () -> tellSender(player, "Player " + targetName + " is not online.", true), 1);
                        if (!accepted) ChatUtil.sendErrorMessage(player, "Player " + targetName + " is not online.");
                    } catch (IllegalPluginAccessException ignored) {
                        ChatUtil.sendErrorMessage(player, "FancyFirework is stopping.");
                    }
                }
            } else {
                ChatUtil.sendErrorMessage(player, "Firework " + key + " not available.");
            }
        } else {
            ChatUtil.sendWarningMessage(player, "/fancyfirework give [key] <amount> <PlayerName>");
            return true;
        }
        return true;
    }

    private void give(Player sender, Player target, AbstractFireWork fireWork, int amount) {
        if (plugin.isStopping() || !target.isOnline()) {
            tellSender(sender, "Player " + target.getName() + " is not online.", true);
            return;
        }
        Inventory inv = target.getInventory();
        ItemStack itemStack = fireWork.getItemStack();
        itemStack.setAmount(amount);
        int capacity = 0;
        for (ItemStack existing : inv.getStorageContents()) {
            if (existing == null || existing.getType().isAir()) capacity += itemStack.getMaxStackSize();
            else if (existing.isSimilar(itemStack)) capacity += existing.getMaxStackSize() - existing.getAmount();
        }
        if (capacity < amount) {
            tellSender(sender, "Item could not be added. The inventory is full!", true);
            return;
        }
        inv.addItem(itemStack);
        ChatUtil.sendNormalMessage(target, amount + "x " + itemStack.getItemMeta().getDisplayName() + ChatUtil.GREEN + " has been added to your inventory");
        if (target != sender) {
            tellSender(sender, amount + "x " + itemStack.getItemMeta().getDisplayName() + ChatUtil.GREEN + " has been added to " + target.getName() + "'s inventory", false);
        }
    }

    private void tellSender(Player sender, String message, boolean error) {
        if (plugin.isStopping()) return;
        try {
            sender.getScheduler().execute(plugin, () -> {
                if (sender.isOnline()) {
                    if (error) ChatUtil.sendErrorMessage(sender, message);
                    else ChatUtil.sendNormalMessage(sender, message);
                }
            }, null, 1);
        } catch (IllegalPluginAccessException ignored) {
            // The plugin was disabled before feedback could be scheduled.
        }
    }

    @Override
    public ArrayList<String> onTabComplete(Player player, Command command, String alias, ArgsParser args) {
        if (player.hasPermission(FancyFirework.MOD_PERMISSION)) {
            int i = args.remaining();
            if (i == 1) {
                ArrayList<String> li = new ArrayList<>();
                plugin.getRegistry().getKeys().forEach((x) -> li.add(x.toString()));
                return li;
            }
            if (player.hasPermission(FancyFirework.ADMIN_PERMISSION)) {
                if (i == 3) {
                    ArrayList<String> li = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach((onlinePlayer) -> li.add(onlinePlayer.getName()));
                    return li;
                }
            }
            return new ArrayList<>();
        }
        return null;
    }

    @Override
    public String getRequiredPermission() {
        return FancyFirework.MOD_PERMISSION;
    }
}
