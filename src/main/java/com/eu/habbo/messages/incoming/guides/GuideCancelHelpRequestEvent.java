package com.eu.habbo.messages.incoming.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuideTour;
import com.eu.habbo.messages.incoming.MessageHandler;

/**
 * Created on 10-10-2015 22:40.
 */
public class GuideCancelHelpRequestEvent extends MessageHandler
{
    @Override
    public void handle() throws Exception
    {
        GuideTour tour = Emulator.getGameEnvironment().getGuideManager().getGuideTourByNoob(this.client.getHabbo());

        if(tour != null)
        {
            tour.end();
            Emulator.getGameEnvironment().getGuideManager().endSession(tour);
        }
    }
}
