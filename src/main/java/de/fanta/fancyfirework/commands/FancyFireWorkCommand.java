package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FancyFireWorkCommand extends SubCommand {
    private final FancyFirework plugin;

    public FancyFireWorkCommand(FancyFirework plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        ChatUtil.sendNormalMessage(sender, "FancyFirework v" + plugin.getDescription().getVersion() + "，作者：" + plugin.getDescription().getAuthors());
        ChatUtil.sendNormalMessage(sender, "输入 /ff help 查看命令帮助。");
        if (sender instanceof Player && sender.hasPermission(FancyFirework.ADMIN_PERMISSION)) {
            ChatUtil.sendNormalMessage(sender, "管理员烟花大全：/ff menu");
        }
        return true;
    }
}
