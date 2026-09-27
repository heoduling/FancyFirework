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
                    ChatUtil.sendErrorMessage(player, "数量必须在 1 到 64 之间。");
                    return true;
                }
                amount = next;

                if (args.hasNext()) {
                    if (!player.hasPermission(FancyFirework.ADMIN_PERMISSION)) {
                        ChatUtil.sendErrorMessage(player, "你没有向其他玩家发放烟花的权限。");
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
                        ChatUtil.sendErrorMessage(player, "玩家 " + giveName + " 不在线。");
                        return true;
                    }
                }
            } else {
                ChatUtil.sendWarningMessage(player, "用法：/fancyfirework give [烟花键] <数量> <玩家名>");
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
                                () -> tellSender(player, "玩家 " + targetName + " 不在线。", true), 1);
                        if (!accepted) ChatUtil.sendErrorMessage(player, "玩家 " + targetName + " 不在线。");
                    } catch (IllegalPluginAccessException ignored) {
                        ChatUtil.sendErrorMessage(player, "FancyFirework 正在停用。");
                    }
                }
            } else {
                ChatUtil.sendErrorMessage(player, "烟花 " + key + " 不存在。");
            }
        } else {
            ChatUtil.sendWarningMessage(player, "用法：/fancyfirework give [烟花键] <数量> <玩家名>");
            return true;
        }
        return true;
    }

    private void give(Player sender, Player target, AbstractFireWork fireWork, int amount) {
        if (plugin.isStopping() || !target.isOnline()) {
            tellSender(sender, "玩家 " + target.getName() + " 不在线。", true);
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
            tellSender(sender, "背包空间不足，无法发放烟花。", true);
            return;
        }
        inv.addItem(itemStack);
        ChatUtil.sendNormalMessage(target, "已收到 " + amount + " 个 " + itemStack.getItemMeta().getDisplayName());
        if (target != sender) {
            tellSender(sender, "已向 " + target.getName() + " 发放 " + amount + " 个 " + itemStack.getItemMeta().getDisplayName(), false);
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
