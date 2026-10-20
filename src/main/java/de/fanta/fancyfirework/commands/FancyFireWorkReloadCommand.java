package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class FancyFireWorkReloadCommand extends SubCommand {
    private final FancyFirework plugin;

    public FancyFireWorkReloadCommand(FancyFirework plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        if (plugin.requestConfigReload()) {
            ChatUtil.sendNormalMessage(sender, "配置重载已开始，完成后自动生效。");
        } else {
            ChatUtil.sendErrorMessage(sender, "配置正在重载或插件即将停用。");
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return FancyFirework.ADMIN_PERMISSION;
    }
}
