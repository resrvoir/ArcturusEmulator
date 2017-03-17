package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

import java.util.List;

public class RoomCategoriesComposer extends MessageComposer
{
    private List<RoomCategory> categories;

    public RoomCategoriesComposer(List<RoomCategory> categories)
    {
        this.categories = categories;
    }

    @Override
    public ServerMessage compose()
    {
        this.response.init(Outgoing.RoomCategoriesComposer);

        this.response.appendInt32(this.categories.size());
        for(RoomCategory category : this.categories)
        {
            this.response.appendInt32(category.getId());
            this.response.appendString(category.getCaption());
            this.response.appendBoolean(true); //Visible
            this.response.appendBoolean(false); //True = Disconnect?
            this.response.appendString(category.getCaption());

            if (category.getCaption().startsWith("${"))
            {
                this.response.appendString("");
            }
            else
            {
                this.response.appendString(category.getCaption());
            }

            this.response.appendBoolean(false);
        }

        return this.response;
    }
}
