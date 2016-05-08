package com.eu.habbo.habbohotel.guilds;

/**
 * Created on 28-1-2016 20:35.
 */
public enum GuildState
{
    OPEN(0),
    LOCKED(1),
    CLOSED(2);

    public final int state;

    GuildState(int state)
    {
        this.state = state;
    }

    public static GuildState valueOf(int state)
    {
        try
        {
            return values()[state];
        }
        catch (Exception e)
        {
            return OPEN;
        }
    }
}
