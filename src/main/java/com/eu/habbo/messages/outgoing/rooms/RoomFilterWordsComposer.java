package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Created on 21-8-2015 12:26.
 */
public class RoomFilterWordsComposer extends MessageComposer
{
    private Room room;

    public RoomFilterWordsComposer(Room room)
    {
        this.room = room;
    }

    @Override
    public ServerMessage compose()
    {
        this.response.init(Outgoing.RoomFilterWordsComposer);
        this.response.appendInt32(this.room.getWordFilterWords().size());

        for(String string : this.room.getWordFilterWords())
        {
            this.response.appendString(string);
        }

        return this.response;
    }
}
