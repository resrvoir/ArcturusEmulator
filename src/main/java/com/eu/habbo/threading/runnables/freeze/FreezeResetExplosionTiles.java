package com.eu.habbo.threading.runnables.freeze;

import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;
import gnu.trove.set.hash.THashSet;

/**
 * Created on 24-1-2015 16:33.
 */
class FreezeResetExplosionTiles implements Runnable
{
    private final THashSet<InteractionFreezeTile> tiles;
    private final Room room;

    public FreezeResetExplosionTiles(THashSet<InteractionFreezeTile> tiles, Room room)
    {
        this.tiles = tiles;
        this.room = room;
    }
    @Override
    public void run()
    {
        for(HabboItem item : this.tiles)
        {
            room.updateItem(item);
        }
    }
}
