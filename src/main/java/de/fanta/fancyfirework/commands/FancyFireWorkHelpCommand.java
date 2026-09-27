package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FancyFireWorkHelpCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        ChatUtil.sendNormalMessage(sender, "FancyFirework 命令帮助（/fancyfirework 可简写为 /ff）：");
        ChatUtil.sendNormalMessage(sender, "/ff help - 查看命令帮助");

        boolean player = sender instanceof Player;
        if (player && sender.hasPermission(FancyFirework.MOD_PERMISSION)) {
            ChatUtil.sendNormalMessage(sender, "/ff give <烟花键> <数量> [玩家名] - 领取烟花；向他人发放还需管理员权限");
        }
        if (sender.hasPermission(FancyFirework.ADMIN_PERMISSION)) {
            if (player) {
                ChatUtil.sendNormalMessage(sender, "/ff menu - 打开管理员烟花大全");
                ChatUtil.sendNormalMessage(sender, "/ff enable、/ff disable - 开关随机烟花生成");
                ChatUtil.sendNormalMessage(sender, "/ff use enable、/ff use disable - 开关烟花使用");
            }
            ChatUtil.sendNormalMessage(sender, "/ff setspawnrate <数值> - 设置随机生成率（数值越大，生成越少）");
            ChatUtil.sendNormalMessage(sender, "/ff reload - 重新读取配置");
        }
        return true;
    }
}
