package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.PlayerSubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class FancyFireworkDisableGlobalCommand extends PlayerSubCommand {

    private final FancyFirework plugin;
    private final boolean enable;

    public FancyFireworkDisableGlobalCommand(FancyFirework plugin, boolean enable) {
        this.plugin = plugin;
        this.enable = enable;
    }

    @Override
    public boolean onCommand(Player player, Command command, String alias, String commandString, ArgsParser args) {
        if (enable) {
            if (plugin.isDisableFireWorkUse()) {
                plugin.setDisableFireWorkUse(false);
                ChatUtil.sendNormalMessage(player, "现已允许使用烟花。");
            } else {
                ChatUtil.sendErrorMessage(player, "烟花使用已经开启。");
                return true;
            }
        } else {
            if (!plugin.isDisableFireWorkUse()) {
                plugin.setDisableFireWorkUse(true);
                ChatUtil.sendNormalMessage(player, "现已禁止使用烟花。");
            } else {
                ChatUtil.sendErrorMessage(player, "烟花使用已经关闭。");
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

