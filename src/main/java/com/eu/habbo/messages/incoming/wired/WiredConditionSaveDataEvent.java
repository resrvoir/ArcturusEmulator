package com.eu.habbo.messages.incoming.wired;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.wired.WiredSavedComposer;

/**
 * Created on 14-12-2014 13:26.
 */
public class WiredConditionSaveDataEvent extends MessageHandler
{
    @Override
    public void handle() throws Exception
    {
        int itemId = this.packet.readInt();

        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

        if(room != null)
        {
            if(room.hasRights(this.client.getHabbo()) || room.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission("acc_anyroomowner") || this.client.getHabbo().hasPermission("acc_moverotate"))
            {
                InteractionWiredCondition condition = room.getRoomSpecialTypes().getCondition(itemId);

                if(condition != null)
                {
                    if(condition.saveData(this.packet))
                    {
                        this.client.sendResponse(new WiredSavedComposer());

                        condition.needsUpdate(true);

                        Emulator.getThreading().run(condition);
                    }
                }
            }
        }
    }
}
