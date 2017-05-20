package com.eu.habbo.habbohotel.achievements;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.outgoing.achievements.AchievementProgressComposer;
import com.eu.habbo.messages.outgoing.achievements.AchievementUnlockedComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.AddUserBadgeComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import com.eu.habbo.plugin.Event;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementLeveledEvent;
import com.eu.habbo.plugin.events.users.achievements.UserAchievementProgressEvent;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AchievementManager
{
    /**
     * All the achievements in the hotel are stored in this map where:
     * String = name of the achievement (Without ACH_ & Roman number.
     * Achievement = Instance of the Achievement class.
     */
    private final THashMap<String, Achievement> achievements;

    private final THashMap<TalentTrackType, LinkedHashMap<Integer, TalentTrackLevel>> talentTrackLevels;

    /**
     * The AchievementManager, shit happens here.
     */
    public AchievementManager()
    {
        this.achievements = new THashMap<String, Achievement>();
        this.talentTrackLevels = new THashMap<TalentTrackType, LinkedHashMap<Integer, TalentTrackLevel>>();
    }

    /**
     * Reloads the achievement manager.
     */
    public void reload()
    {
        long millis = System.currentTimeMillis();
        synchronized (this.achievements)
        {
            this.achievements.clear();

            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection())
            {
                try (Statement statement = connection.createStatement(); ResultSet set = statement.executeQuery("SELECT * FROM achievements"))
                {
                    while (set.next())
                    {
                        if (!this.achievements.containsKey(set.getString("name")))
                        {
                            this.achievements.put(set.getString("name"), new Achievement(set));
                        }
                        else
                        {
                            this.achievements.get(set.getString("name")).addLevel(new AchievementLevel(set));
                        }
                    }
                }
                catch (SQLException e)
                {
                    Emulator.getLogging().logSQLException(e);
                }
                catch (Exception e)
                {
                    Emulator.getLogging().logErrorLine(e);
                }


                synchronized (this.talentTrackLevels)
                {
                    this.talentTrackLevels.clear();

                    try (Statement statement = connection.createStatement(); ResultSet set = statement.executeQuery("SELECT * FROM achievements_talents ORDER BY level ASC"))
                    {
                        while (set.next())
                        {
                            TalentTrackLevel level = new TalentTrackLevel(set);

                            if (!this.talentTrackLevels.containsKey(level.type))
                            {
                                this.talentTrackLevels.put(level.type, new LinkedHashMap<Integer, TalentTrackLevel>());
                            }

                            this.talentTrackLevels.get(level.type).put(level.level, level);
                        }
                    }
                }
            }
            catch (SQLException e)
            {
                Emulator.getLogging().logSQLException(e);
                Emulator.getLogging().logErrorLine("Achievement Manager -> Failed to load!");
                return;
            }
        }

        Emulator.getLogging().logStart("Achievement Manager -> Loaded! ("+(System.currentTimeMillis() - millis)+" MS)");
    }

    /**
     * Find an achievement by name.
     * @param name The achievement to find.
     * @return The achievement
     */
    public Achievement getAchievement(String name)
    {
        return this.achievements.get(name);
    }

    /**
     * Find an achievement by id
     * @param id The achievement id to find.
     * @return The achievement
     */
    public Achievement getAchievement(int id)
    {
        synchronized (this.achievements)
        {
            for (Map.Entry<String, Achievement> set : this.achievements.entrySet())
            {
                if (set.getValue().id == id)
                {
                    return set.getValue();
                }
            }
        }

        return null;
    }

    public THashMap<String, Achievement> getAchievements()
    {
        return this.achievements;
    }

    public static void progressAchievement(int habboId, Achievement achievement)
    {
        progressAchievement(habboId, achievement, 1);
    }

    public static void progressAchievement(int habboId, Achievement achievement, int amount)
    {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(habboId);

        if (habbo != null)
        {
            progressAchievement(habbo, achievement, amount);
        }
        else
        {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement("" +
                         "INSERT INTO users_achievements_queue (user_id, achievement_id, amount) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE amount = amount + ?"))
            {
                statement.setInt(1, habboId);
                statement.setInt(2, achievement.id);
                statement.setInt(3, amount);
                statement.setInt(4, amount);
                statement.execute();
            }
            catch (SQLException e)
            {
                Emulator.getLogging().logSQLException(e);
            }
        }
    }


    /**
     * Progresses an Habbo's achievement by 1.
     * @param habbo The Habbo whose achievement should be progressed.
     * @param achievement The Achievement to be progressed.
     */
    public static void progressAchievement(Habbo habbo, Achievement achievement)
    {
        progressAchievement(habbo, achievement, 1);
    }

    /**
     * Progresses an Habbo's achievement by an given amount.
     * @param habbo The Habbo whose achievement should be progressed.
     * @param achievement The Achievement to be progressed.
     * @param amount The amount that should be progressed.
     */
    public static void progressAchievement(Habbo habbo, Achievement achievement, int amount)
    {
        if (achievement == null)
            return;

        if (habbo == null)
            return;

        if (!habbo.isOnline())
            return;

        int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);

        if(currentProgress == -1)
        {
            currentProgress = 0;
            createUserEntry(habbo, achievement);
            habbo.getHabboStats().setProgress(achievement, 0);
        }

        if(Emulator.getPluginManager().isRegistered(UserAchievementProgressEvent.class, true))
        {
            Event userAchievementProgressedEvent = new UserAchievementProgressEvent(habbo, achievement, amount);
            Emulator.getPluginManager().fireEvent(userAchievementProgressedEvent);

            if(userAchievementProgressedEvent.isCancelled())
                return;
        }

        int currentLevelId = 0;
        int nextLevelId = 0;

        AchievementLevel currentLevel = achievement.getLevelForProgress(currentProgress);

        if(currentLevel != null)
        {
            currentLevelId = currentLevel.level;

            if(currentLevel.level == achievement.levels.size() && currentProgress >= currentLevel.progress) //Maximum achievement gotten.
                return;
        }

        AchievementLevel nextLevel = achievement.getLevelForProgress(currentProgress + amount);

        if(nextLevel != null)
        {
            nextLevelId = nextLevel.level;
        }

        habbo.getHabboStats().setProgress(achievement, currentProgress + amount);
        habbo.getClient().sendResponse(new AchievementProgressComposer(habbo, achievement));

        // If we are on the same level
        if(currentLevelId != nextLevelId)
        {
            if(Emulator.getPluginManager().isRegistered(UserAchievementLeveledEvent.class, true))
            {
                Event userAchievementLeveledEvent = new UserAchievementLeveledEvent(habbo, achievement, currentLevel, nextLevel);
                Emulator.getPluginManager().fireEvent(userAchievementLeveledEvent);

                if(userAchievementLeveledEvent.isCancelled())
                    return;
            }

            habbo.getClient().sendResponse(new AchievementUnlockedComposer(habbo, achievement));

            if(nextLevel.rewardCurrency >= 0 && nextLevel.rewardAmount > 0)
            {
                habbo.givePoints(nextLevel.rewardCurrency, nextLevel.rewardAmount);
            }

            //Exception could possibly arise when the user disconnects while being in tour.
            //The achievement is then progressed but the user is already disposed so fetching
            //the badge would result in an nullpointer exception. This is normal behaviour.
            HabboBadge badge = null;
            try
            {
                badge = habbo.getHabboInventory().getBadgesComponent().getBadge(("ACH_" + achievement.name + currentLevelId).toLowerCase());
            }
            catch (Exception e)
            {
                return;
            }

            if (badge != null)
            {
                badge.setCode("ACH_" + achievement.name + nextLevelId);
                badge.needsInsert(false);
                badge.needsUpdate(true);
            }
            else
            {
                badge = new HabboBadge(0, "ACH_" + achievement.name + nextLevelId, 0, habbo);
                habbo.getClient().sendResponse(new AddUserBadgeComposer(badge));
                badge.needsInsert(true);
                badge.needsUpdate(true);
                habbo.getHabboInventory().getBadgesComponent().addBadge(badge);
            }

            Emulator.getThreading().run(badge);

            if(badge.getSlot() > 0)
            {
                if(habbo.getHabboInfo().getCurrentRoom() != null)
                {
                    habbo.getHabboInfo().getCurrentRoom().sendComposer(new UserBadgesComposer(habbo.getHabboInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()).compose());
                }
            }

            habbo.getHabboStats().addAchievementScore(nextLevel.points);

            if (habbo.getHabboInfo().getCurrentRoom() != null)
            {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            }
        }
    }


    /**
     * Checks wether the given Habbo has achieved a certain Achievement.
     * @param habbo The Habbo to check.
     * @param achievement The Achievement to check.
     * @return True when the given Habbo has achieved the Achievement.
     */
    public static boolean hasAchieved(Habbo habbo, Achievement achievement)
    {
        int currentProgress = habbo.getHabboStats().getAchievementProgress(achievement);

        if(currentProgress == -1)
        {
            return false;
        }

        AchievementLevel level = achievement.getLevelForProgress(currentProgress);
        AchievementLevel nextLevel = achievement.levels.get(level.level + 1);

        if(level == null)
            return false;

        if (nextLevel == null && currentProgress >= level.progress)
        {
            return true;
        }

        return false;
    }

    /**
     * Creates an new Achievement entry in the database.
     * @param habbo The Habbo the achievement should be saved for.
     * @param achievement The Achievement that should be inserted.
     */
    public static void createUserEntry(Habbo habbo, Achievement achievement)
    {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users_achievements (user_id, achievement_name, progress) VALUES (?, ?, ?)"))
        {
            statement.setInt(1, habbo.getHabboInfo().getId());
            statement.setString(2, achievement.name);
            statement.setInt(3, 1);
            statement.execute();
        }
        catch (SQLException e)
        {
            Emulator.getLogging().logSQLException(e);
        }
    }

    /**
     * Saves all the Achievements for the given Habbo to the database.
     * @param habbo The Habbo whose Achievements should be saved.
     */
    public static void saveAchievements(Habbo habbo)
    {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE users_achievements SET progress = ? WHERE achievement_name = ? AND user_id = ? LIMIT 1"))
        {
            statement.setInt(3, habbo.getHabboInfo().getId());
            for(Map.Entry<Achievement, Integer> map : habbo.getHabboStats().getAchievementProgress().entrySet())
            {
                statement.setInt(1, map.getValue());
                statement.setString(2, map.getKey().name);
                statement.addBatch();
            }
            statement.executeBatch();
        }
        catch (SQLException e)
        {
            Emulator.getLogging().logSQLException(e);
        }
    }

    /**
     *
     * @param type
     * @return
     */
    public LinkedHashMap<Integer, TalentTrackLevel> getTalenTrackLevels(TalentTrackType type)
    {
        return this.talentTrackLevels.get(type);
    }
}
