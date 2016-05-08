package com.eu.habbo.plugin.events.games;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.plugin.Event;

/**
 * Created on 28-8-2015 11:04.
 */
public abstract class GameEvent extends Event
{
    /**
     * The Game instance that this event applies to.
     */
    public final Game game;

    /**
     * @param game The Game instance that this event applies to.
     */
    public GameEvent(Game game)
    {
        this.game = game;
    }
}
