package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Created on 29-11-2014 16:00.
 */
public class PetNameErrorComposer extends MessageComposer
{
    public static final int NAME_OK = 0;
    public static final int NAME_TO_LONG = 1;
    public static final int NAME_TO_SHORT = 2;
    public static final int FORBIDDEN_CHAR = 3;
    public static final int FORBIDDEN_WORDS = 4;

    private final int type;
    private final String value;

    public PetNameErrorComposer(int type, String value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public ServerMessage compose()
    {
        this.response.init(Outgoing.PetNameErrorComposer);
        this.response.appendInt32(this.type);
        this.response.appendString(this.value);
        return this.response;
    }
}
