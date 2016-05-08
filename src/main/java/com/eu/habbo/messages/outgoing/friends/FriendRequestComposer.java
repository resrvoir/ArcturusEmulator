package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/**
 * Created on 25-8-2014 18:25.
 */
public class FriendRequestComposer extends MessageComposer {

    private final Habbo habbo;

    public FriendRequestComposer(Habbo habbo)
    {
        this.habbo = habbo;
    }

    @Override
    public ServerMessage compose() {
        this.response.init(Outgoing.FriendRequestComposer);

        this.response.appendInt32(this.habbo.getHabboInfo().getId());
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendString(this.habbo.getHabboInfo().getLook());

        return this.response;
    }
}
