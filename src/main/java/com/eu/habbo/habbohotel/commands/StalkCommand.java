package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;

/**
 * Created on 24-10-2015 15:22.
 */
public class StalkCommand extends Command
{
    public StalkCommand()
    {
        super.permission = "cmd_stalk";
        super.keys = Emulator.getTexts().getValue("commands.keys.cmd_stalk").split(";");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception
    {
        if(gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null)
            return true;

        if(params.length >= 2)
        {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(params[1]);

            if(habbo == null)
            {
                gameClient.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.error.cmd_stalk.not_found").replace("%user%", params[1]), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.ALERT)));
                return true;
            }

            if(habbo.getHabboInfo().getCurrentRoom() == null)
            {
                gameClient.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.error.cmd_stalk.not_room").replace("%user%", params[1]), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.ALERT)));
                return true;
            }

            if(gameClient.getHabbo().getHabboInfo().getUsername().equals(habbo.getHabboInfo().getUsername()))
            {
                gameClient.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.generic.cmd_stalk.self").replace("%user%", params[1]), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.ALERT)));
                return true;
            }

            if(gameClient.getHabbo().getHabboInfo().getCurrentRoom() == habbo.getHabboInfo().getCurrentRoom())
            {
                gameClient.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.generic.cmd_stalk.same_room").replace("%user%", params[1]), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.ALERT)));
                return true;
            }

            gameClient.sendResponse(new ForwardToRoomComposer(habbo.getHabboInfo().getCurrentRoom().getId()));
            return true;
        }
        else
        {
            gameClient.sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(Emulator.getTexts().getValue("commands.error.cmd_summon.forgot_username"), gameClient.getHabbo(), gameClient.getHabbo(), RoomChatMessageBubbles.ALERT)));
            return true;
        }
    }
}
