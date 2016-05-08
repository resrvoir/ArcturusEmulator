package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/**
 * Created on 29-12-2014 20:44.
 */
public class SnowWarsUserEnteredArenaComposer extends MessageComposer
{
    private final int type;

    public SnowWarsUserEnteredArenaComposer(int type)
    {
        this.type =type;
    }
    @Override
    public ServerMessage compose()
    {
        this.response.init(3425);

        if(type == 1)
        {
            this.response.appendInt32(1); //userId
            this.response.appendString("Admin");
            this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-62");
            this.response.appendString("m");
            this.response.appendInt32(1); //team
        }
        else
        {
            this.response.appendInt32(0); //userId
            this.response.appendString("Droppy");
            this.response.appendString("ca-1807-64.lg-275-78.hd-3093-1.hr-802-42.ch-3110-65-62.fa-1211-62");
            this.response.appendString("m");
            this.response.appendInt32(2); //team
        }
        return this.response;
    }
}
