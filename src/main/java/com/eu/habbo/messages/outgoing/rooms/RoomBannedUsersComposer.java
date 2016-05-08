package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomBan;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.set.hash.THashSet;

import java.util.NoSuchElementException;

/**
 * Created on 30-8-2015 11:08.
 */
public class RoomBannedUsersComposer extends MessageComposer
{
    private final Room room;

    public RoomBannedUsersComposer(Room room)
    {
        this.room = room;
    }

    @Override
    public ServerMessage compose()
    {
        int timeStamp = Emulator.getIntUnixTimestamp();

        THashSet<RoomBan> roomBans = new THashSet<RoomBan>();

        TIntObjectIterator<RoomBan> iterator = this.room.getBannedHabbos().iterator();

        for(int i = this.room.getBannedHabbos().size(); i-- > 0;)
        {
            try
            {
                iterator.advance();

                if(iterator.value().endTimestamp > timeStamp)
                    roomBans.add(iterator.value());
            }
            catch (NoSuchElementException e)
            {
                break;
            }
        }

        if(roomBans.isEmpty())
            return null;

        this.response.init(Outgoing.RoomBannedUsersComposer);
        this.response.appendInt32(this.room.getId());
        this.response.appendInt32(roomBans.size());

        for(RoomBan ban : roomBans)
        {
            this.response.appendInt32(ban.userId);
            this.response.appendString(ban.username);
        }

        return this.response;
    }
}
