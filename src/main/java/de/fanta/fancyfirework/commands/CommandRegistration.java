package de.fanta.fancyfirework.commands;

import de.fanta.fancyfirework.FancyFirework;
import de.fanta.fancyfirework.utils.ChatUtil;
import de.iani.cubesideutils.bukkit.commands.CommandExceptionHandler;
import de.iani.cubesideutils.bukkit.commands.CommandRouter;
import de.iani.cubesideutils.bukkit.commands.exceptions.CommandRouterException;
import de.iani.cubesideutils.bukkit.commands.exceptions.DisallowsCommandBlockException;
import de.iani.cubesideutils.bukkit.commands.exceptions.IllegalSyntaxException;
import de.iani.cubesideutils.bukkit.commands.exceptions.NoPermissionException;
import de.iani.cubesideutils.bukkit.commands.exceptions.NoPermissionForPathException;
import de.iani.cubesideutils.bukkit.commands.exceptions.RequiresPlayerException;

public record CommandRegistration(FancyFirework plugin, AdminFireworkMenuCommand adminMenu) {

    public void registerCommands() {
        CommandRouter fancyFireworkRouter = new CommandRouter(plugin.getCommand("fancyfirework"), new CommandExceptionHandler() {
            @Override
            public void sendExceptionMessage(CommandRouterException error) {
                String message;
                if (error instanceof NoPermissionException || error instanceof NoPermissionForPathException) {
                    message = "你没有执行此命令的权限。";
                } else if (error instanceof RequiresPlayerException) {
                    message = "此命令只能由玩家执行。";
                } else if (error instanceof DisallowsCommandBlockException) {
                    message = "命令方块不能执行此命令。";
                } else {
                    message = "命令执行失败，请检查参数。";
                }
                ChatUtil.sendErrorMessage(error.getSender(), message);
            }

            @Override
            public boolean handleIllegalSyntax(IllegalSyntaxException error) {
                ChatUtil.sendWarningMessage(error.getSender(), "命令格式错误。");
                return true;
            }
        });
        fancyFireworkRouter.addCommandMapping(new FancyFireWorkCommand(plugin));
        fancyFireworkRouter.addCommandMapping(new FancyFireWorkHelpCommand(), "help");
        fancyFireworkRouter.addCommandMapping(new EnableToggleCommand(plugin, true), "enable");
        fancyFireworkRouter.addCommandMapping(new EnableToggleCommand(plugin, false), "disable");
        fancyFireworkRouter.addCommandMapping(new FancyFireworkDisableGlobalCommand(plugin, true), "use", "enable");
        fancyFireworkRouter.addCommandMapping(new FancyFireworkDisableGlobalCommand(plugin, false), "use", "disable");
        fancyFireworkRouter.addCommandMapping(new SetSpawnRateCommand(plugin), "setSpawnRate");
        fancyFireworkRouter.addCommandMapping(new FireWorkGiveCommand(plugin), "give");
        fancyFireworkRouter.addCommandMapping(adminMenu, "menu");
        fancyFireworkRouter.addCommandMapping(new FancyFireWorkReloadCommand(plugin), "reload");

    }
}
