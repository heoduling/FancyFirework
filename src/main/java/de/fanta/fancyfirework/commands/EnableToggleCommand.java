package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.PlayerSubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class EnableToggleCommand extends PlayerSubCommand {

    private final FancyFirework plugin;
    private final boolean enable;

    public EnableToggleCommand(FancyFirework plugin, boolean enable) {
        this.plugin = plugin;
        this.enable = enable;
    }

    @Override
    public boolean onCommand(Player player, Command command, String alias, String commandString, ArgsParser args) {
        if (enable) {
            if (!plugin.getFireWorkWorks().enabled()) {
                plugin.getFireWorkWorks().setEnabled(true);
                ChatUtil.sendNormalMessage(player, "已开启随机烟花生成。");
            } else {
                ChatUtil.sendErrorMessage(player, "随机烟花生成已经开启。");
                return true;
            }
        } else {
            if (plugin.getFireWorkWorks().enabled()) {
                plugin.getFireWorkWorks().setEnabled(false);
                ChatUtil.sendNormalMessage(player, "已关闭随机烟花生成。");
            } else {
                ChatUtil.sendErrorMessage(player, "随机烟花生成已经关闭。");
                return true;
            }
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return FancyFirework.ADMIN_PERMISSION;
    }
}

