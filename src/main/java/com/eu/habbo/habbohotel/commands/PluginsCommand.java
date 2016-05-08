package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.plugin.HabboPlugin;

/**
 * Created on 17-4-2016 14:02.
 */
public class PluginsCommand extends Command
{
    public PluginsCommand()
    {
        super.permission = "cmd_plugins";
        super.keys = Emulator.getTexts().getValue("commands.keys.cmd_plugins").split(";");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception
    {
        String message = "Plugins (" + Emulator.getPluginManager().getPlugins().size() + ")\r";

        for (HabboPlugin plugin : Emulator.getPluginManager().getPlugins())
        {
            message += "\r" + plugin.configuration.name + " By " + plugin.configuration.author;
        }

        gameClient.sendResponse(new GenericAlertComposer(message));

        return true;
    }
}
