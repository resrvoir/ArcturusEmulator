package com.eu.habbo.habbohotel.items.interactions.games.freeze.scoreboards;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created on 23-1-2015 19:29.
 */
public class InteractionFreezeScoreboardRed extends InteractionFreezeScoreboard
{
    public static final GameTeamColors TEAM_COLOR = GameTeamColors.BLUE;

    public InteractionFreezeScoreboardRed(ResultSet set, Item baseItem) throws SQLException
    {
        super(set, baseItem, TEAM_COLOR);
    }

    public InteractionFreezeScoreboardRed(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells)
    {
        super(id, userId, item, extradata, limitedStack, limitedSells, TEAM_COLOR);
    }
}
