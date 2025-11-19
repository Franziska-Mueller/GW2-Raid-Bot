package me.cbitler.raidbot.server_settings;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.database.Database;
import me.cbitler.raidbot.database.QueryResult;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class representing different server settings.
 *
 * @author Franziska Mueller
 */
public class ServerSettings {
    private static final Logger log = LogManager.getLogger(ServerSettings.class);

    public enum ChannelType { ARCHIVE, FRACTALS, AUTOEVENTS };

    static HashMap<String, String> raidLeaderRoleCache = new HashMap<>();
    static HashMap<String, String> fractalCreatorRoleCache = new HashMap<>();

    static HashMap<String, String> fractalChannelCache = new HashMap<>();
    static HashMap<String, String> archiveChannelCache = new HashMap<>();
    static HashMap<String, String> autoEventsChannelCache = new HashMap<>();

    static HashMap<String, SortedMap<String, List<String>>> permittedDiscordRoles = new HashMap<>();
    static HashMap<String, SortedMap<String, List<RaidRole>>> roleTemplates = new HashMap<>();
    static String roleTemplatesDelimiter = "#";


    /**
     * Get the raid leader role for a specific server.
     * This works by caching the role once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the role that is considered the raid leader for that server
     */
    public static String getRaidLeaderRole(String serverId) {
        if (raidLeaderRoleCache.get(serverId) != null) {
            return raidLeaderRoleCache.get(serverId);
        } else {
            try {
                Database db = RaidBot.getInstance().getDatabase();
                QueryResult results = db.query("SELECT `raid_leader_role` FROM `serverSettings` WHERE `serverId` = ?",
                        new String[]{serverId});
                if (results.getResults().next()) {
                    raidLeaderRoleCache.put(serverId, results.getResults().getString("raid_leader_role"));
                    return raidLeaderRoleCache.get(serverId);
                } else {
                    return "Raid Leader";
                }
            } catch (Exception e) {
                return "Raid Leader";
            }
        }
    }


    /**
     * Set the raid leader role for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param role The role name
     */
    public static void setRaidLeaderRole(String serverId, String role) {
        raidLeaderRoleCache.put(serverId, role);
        Database db = RaidBot.getInstance().getDatabase();
        try {
            db.update("INSERT INTO `serverSettings` (`serverId`,`raid_leader_role`) VALUES (?,?)",
                    new String[] { serverId, role});
        } catch (SQLException e) {
            //TODO: There is probably a much better way of doing this
            try {
                db.update("UPDATE `serverSettings` SET `raid_leader_role` = ? WHERE `serverId` = ?",
                        new String[] { role, serverId });
            } catch (SQLException e1) {
                // Not much we can do if there is also an insert error
                log.error("Error writing server setting to database on setRaidLeaderRole.", e1);
            }
        }
    }


    /**
     * Get the fractal creator role for a specific server.
     * This works by caching the role once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the role that is considered the fractal creator for that server
     */
    public static String getFractalCreatorRole(String serverId) {
        if (fractalCreatorRoleCache.get(serverId) != null) {
            return fractalCreatorRoleCache.get(serverId);
        } else {
            try {
                Database db = RaidBot.getInstance().getDatabase();
                QueryResult results = db.query("SELECT `fractal_creator_role` FROM `serverSettings` WHERE `serverId` = ?",
                        new String[]{serverId});
                if (results.getResults().next()) {
                    fractalCreatorRoleCache.put(serverId, results.getResults().getString("fractal_creator_role"));
                    return fractalCreatorRoleCache.get(serverId);
                } else {
                    return "Fractal Creator";
                }
            } catch (Exception e) {
                return "Fractal Creator";
            }
        }
    }

    /**
     * Set the fractal creator role for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param role The role name
     */
    public static void setFractalCreatorRole(String serverId, String role) {
        fractalCreatorRoleCache.put(serverId, role);
        Database db = RaidBot.getInstance().getDatabase();
        try {
            db.update("INSERT INTO `serverSettings` (`serverId`,`fractal_creator_role`) VALUES (?,?)",
                    new String[] { serverId, role});
        } catch (SQLException e) {
            //TODO: There is probably a much better way of doing this
            try {
                db.update("UPDATE `serverSettings` SET `fractal_creator_role` = ? WHERE `serverId` = ?",
                        new String[] { role, serverId });
            } catch (SQLException e1) {
                // Not much we can do if there is also an insert error
                log.error("Error writing server setting to database on setFractalCreatorRole.", e1);
            }
        }
    }


    /**
     * Set a channel for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param channel The channel name
     * @return 0: valid channel, 1: no channel found, 2: cannot write in channel
     */
    public static int setChannel(String serverId, String channel, ChannelType type) {
        // check if channel exists
        if (checkChannel(serverId, channel) == false)
            return 1;

        // check if a message can be written into the channel
        Guild guild = RaidBot.getInstance().getServer(serverId);
        List<TextChannel> channels = guild.getTextChannelsByName(getChannel(serverId, type), true);
        if(channels.size() > 0) {
            // We always go with the first channel if there is more than one
            if (channels.get(0).canTalk() == false)
                return 2;
            // TODO: check if we have "embed links" permission required to post embedded event messages
        }

        String dbField = "";
        if (type == ChannelType.ARCHIVE)
        {
            archiveChannelCache.put(serverId, channel);
            dbField = "archive_channel";
        }
        else if (type == ChannelType.FRACTALS)
        {
            fractalChannelCache.put(serverId,  channel);
            dbField = "fractal_channel";
        }
        else if (type == ChannelType.AUTOEVENTS)
        {
            autoEventsChannelCache.put(serverId,  channel);
            dbField = "auto_events_channel";
        }

        Database db = RaidBot.getInstance().getDatabase();
        try {
            db.update("INSERT INTO `serverSettings` (`serverId`,`" + dbField + "`) VALUES (?,?)",
                    new String[] { serverId, channel});
        } catch (SQLException e) {
            //TODO: There is probably a much better way of doing this
            try {
                db.update("UPDATE `serverSettings` SET `" + dbField + "` = ? WHERE `serverId` = ?",
                        new String[] { channel, serverId });
            } catch (SQLException e1) {
                // Not much we can do if there is also an update error
                log.error("Error writing server setting to database on setChannel.", e1);
            }
        }
        return 0;
    }

    /**
     * Get a channel for a specific server.
     * This works by caching the channel once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the channel that is considered the archive channel for that server
     */
    private static String getChannel(String serverId, ChannelType type) {
        String cached = null;
        String dbField = "";
        if (type == ChannelType.ARCHIVE)
        {
            cached = archiveChannelCache.get(serverId);
            dbField = "archive_channel";
        }
        else if (type == ChannelType.FRACTALS)
        {
            cached = fractalChannelCache.get(serverId);
            dbField = "fractal_channel";
        }
        else if (type == ChannelType.AUTOEVENTS)
        {
            autoEventsChannelCache.get(serverId);
            dbField = "auto_events_channel";
        }

        if (cached != null) {
            return cached;
        } else {
            try {
                Database db = RaidBot.getInstance().getDatabase();
                QueryResult results = db.query("SELECT `" + dbField + "` FROM `serverSettings` WHERE `serverId` = ?",
                        new String[]{serverId});
                if (results.getResults().next()) {
                    String result = results.getResults().getString(dbField);
                    if (result != null) {
                        if (type == ChannelType.ARCHIVE)
                            archiveChannelCache.put(serverId, result);
                        else if (type == ChannelType.ARCHIVE)
                            fractalChannelCache.put(serverId, result);
                        else if (type == ChannelType.AUTOEVENTS)
                            autoEventsChannelCache.get(serverId);
                        return result;
                    }
                    else
                        return "dummy-channel";
                } else {
                    return "dummy-channel";
                }
            } catch (Exception e) {
                return "dummy-channel";
            }
        }
    }

    /**
     * Set the fractal announcement channel for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param channel The channel name
     * @return 0: valid channel, 1: no channel found, 2: cannot write in channel
     */
    public static int setFractalChannel(String serverId, String channel) {
        return setChannel(serverId, channel, ChannelType.FRACTALS);
    }

    /**
     * Get the fractal announcement channel for a specific server.
     * This works by caching the channel once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the channel that is considered the fractal announcement channel for that server
     */
    public static String getFractalChannel(String serverId) {
        return getChannel(serverId, ChannelType.FRACTALS);
    }

    /**
     * Set the archive channel for a server. This also updates it in SQLite
     * @param serverId The server ID
     * @param channel The channel name
     * @return 0: valid channel, 1: no channel found, 2: cannot write in channel
     */
    public static int setArchiveChannel(String serverId, String channel) {
        return setChannel(serverId, channel, ChannelType.ARCHIVE);
    }

    /**
     * Get the archive channel for a specific server.
     * This works by caching the channel once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the channel that is considered the archive channel for that server
     */
    public static String getArchiveChannel(String serverId) {
        return getChannel(serverId, ChannelType.ARCHIVE);
    }

    /**
     * Get the auto events channel for a specific server.
     * This works by caching the channel once it's retrieved once, and returning the default if a server hasn't set one.
     * @param serverId the ID of the server
     * @return The name of the channel that is considered the auto events channel for that server
     */
    public static String getAutoEventsChannel(String serverId) {
        return getChannel(serverId, ChannelType.AUTOEVENTS);
    }


    /** FUNCTIONS FOR ROLE GROUPS */

    public static void removeRoleGroup(String serverId, int groupId) {
        SortedMap<String, List<String>> serverRoleGroups = permittedDiscordRoles.get(serverId);
        if (serverRoleGroups != null)
        {
            Iterator<String> groupNames = serverRoleGroups.keySet().iterator();
            try {
                while (groupId >= 0)
                {
                    groupNames.next();
                    groupId--;
                }
                groupNames.remove();
            } catch (Exception e) { }
            updateRoleGroupsDB(serverId);
        }
    }


    public static boolean addRoleGroup(String serverId, String groupName, List<String> roles) {
        // first check if all roles exist on this server
        for (String roleName : roles)
        {
            if (checkRole(serverId, roleName) == false)
                return false;
        }

        // add role group
        SortedMap<String, List<String>> serverRoleGroups = permittedDiscordRoles.get(serverId);
        if (serverRoleGroups == null)
            serverRoleGroups = new TreeMap<>();
        serverRoleGroups.put(groupName, roles);
        permittedDiscordRoles.put(serverId, serverRoleGroups);
        updateRoleGroupsDB(serverId);

        return true;
    }


    private static void updateRoleGroupsDB(String serverId) {
        SortedMap<String, List<String>> serverRoleGroups = permittedDiscordRoles.get(serverId);
        Database db = RaidBot.getInstance().getDatabase();
        if (serverRoleGroups == null)
        {
            try {
                db.update("UPDATE `serverSettings` SET `predef_role_groups` = NULL WHERE `serverId` = ?",
                        new String[]{ serverId });
            } catch (SQLException e_remove) { }
        }
        else
        {
            String dbString = convertRoleGroupsToString(serverRoleGroups);
            try {
                db.update("INSERT INTO `serverSettings` (`serverId`,`predef_role_groups`) VALUES (?,?)",
                    new String[] { serverId, dbString});
            } catch (SQLException e) {
                //TODO: There is probably a much better way of doing this
                try {
                    db.update("UPDATE `serverSettings` SET `predef_role_groups` = ? WHERE `serverId` = ?",
                            new String[] { dbString, serverId });
                } catch (SQLException e1) {
                    // Not much we can do if there is also an update error
                    log.error("Error writing server setting to database on updateRoleGroupsDB.", e1);
                }
            }
        }
    }

    private static String convertRoleGroupsToString(SortedMap<String, List<String>> roleGroups) {
        String result = "";
        if (roleGroups == null)
            return result;
        Iterator<String> groupNamesIt = roleGroups.keySet().iterator();
        Iterator<List<String>> roleNamesIt = roleGroups.values().iterator();
        for (int g = 0; g < roleGroups.size(); g++)
        {
            result += groupNamesIt.next() + ";";
            List<String> roles = roleNamesIt.next();
            for (int r = 0; r < roles.size(); r++)
            {
                result += roles.get(r);
                if (r < roles.size() - 1)
                    result += ",";
            }

            if (g < roleGroups.size() - 1)
                result += "/";
        }
        return result;
    }

    private static SortedMap<String, List<String>> convertRoleGroupsFromString(String input) {
        SortedMap<String, List<String>> result = new TreeMap<>();
        String[] groupSplits = input.split("/");
        for (int g = 0; g < groupSplits.length; g++)
        {
            String[] nameSplits = groupSplits[g].split(";");
            if (nameSplits.length != 2)
                continue;
            String[] roleSplits = nameSplits[1].split(",");
            List<String> roleNames = new ArrayList<String>();
            for (int r = 0; r < roleSplits.length; r++)
                roleNames.add(roleSplits[r]);
            result.put(nameSplits[0], roleNames);
        }
        return result;
    }

    /**
     * Loads the predefined role groups for a server from the database if not already cached
     * @param serverId
     */
    private static void loadPredefRoleGroups(String serverId) {
        if (permittedDiscordRoles.get(serverId) != null)
            return;
        try {
            Database db = RaidBot.getInstance().getDatabase();
            QueryResult results = db.query("SELECT `predef_role_groups` FROM `serverSettings` WHERE `serverId` = ?",
                    new String[]{serverId});
            if (results.getResults().next()) {
                String result = results.getResults().getString("predef_role_groups");
                if (result != null) {
                    // construct sorted map
                    permittedDiscordRoles.put(serverId, convertRoleGroupsFromString(result));
                }
            }
        } catch (Exception e) { }
    }


    /**
     * Returns the set of predefined role groups on this server
     * @param serverId
     * @return list of predefined role groups
     */
    public static Set<String> getPredefGroupNames(String serverId) {
        loadPredefRoleGroups(serverId);
        SortedMap<String, List<String>> permRolesServer = permittedDiscordRoles.get(serverId);
        if (permRolesServer == null)
            return new HashSet<String>();
        return permRolesServer.keySet();
    }

    /**
     * Returns the list of discord roles corresponding to a predefined group name
     * @param serverId
     * @param groupId
     * @return list of discord roles
     */
    public static List<String> getPredefGroupRoles(String serverId, int groupId) {
        loadPredefRoleGroups(serverId);
        SortedMap<String, List<String>> permRolesServer = permittedDiscordRoles.get(serverId);
        List<String> result = new ArrayList<String>();
        if (permRolesServer == null)
            return result;
        Iterator<List<String>> it = permRolesServer.values().iterator();
        try {
            for (int i = 0; i < groupId; i++)
                it.next();
            return it.next();
        } catch (Exception exc) {
            return result;
        }
    }

    /**
     * Returns a list of lists of discord roles corresponding to a all group names
     * @param serverId
     * @return list of lists of discord roles
     */
    public static List<List<String>> getAllPredefGroupRoles(String serverId) {
        loadPredefRoleGroups(serverId);
        SortedMap<String, List<String>> permRolesServer = permittedDiscordRoles.get(serverId);
        List<List<String>> result = new ArrayList<>();
        if (permRolesServer == null)
            return result;
        Iterator<List<String>> it = permRolesServer.values().iterator();
        while (it.hasNext())
            result.add(it.next());
        return result;
    }

    /*********************************/

    /** FUNCTIONS FOR ROLE TEMAPLTES */

    public static void removeRoleTemplate(String serverId, int templateId) {
        SortedMap<String, List<RaidRole>> serverRoleTemplates = roleTemplates.get(serverId);
        if (serverRoleTemplates != null)
        {
            Iterator<String> templateNames = serverRoleTemplates.keySet().iterator();
            try {
                while (templateId >= 0)
                {
                    templateNames.next();
                    templateId--;
                }
                templateNames.remove();
            } catch (Exception e) { }
            updateRoleTemplatesDB(serverId);
        }
    }

    public static boolean addRoleTemplate(String serverId, String templateName, List<RaidRole> roles) {
        // add role template
        SortedMap<String, List<RaidRole>> serverRoleTemplates = roleTemplates.get(serverId);
        if (serverRoleTemplates == null)
            serverRoleTemplates = new TreeMap<>();
        serverRoleTemplates.put(templateName, roles);
        roleTemplates.put(serverId, serverRoleTemplates);
        updateRoleTemplatesDB(serverId);

        return true;
    }


    private static void updateRoleTemplatesDB(String serverId) {
        SortedMap<String, List<RaidRole>> serverRoleTemplates = roleTemplates.get(serverId);
        Database db = RaidBot.getInstance().getDatabase();
        if (serverRoleTemplates == null)
        {
            try {
                db.update("UPDATE `serverSettings` SET `role_templates` = NULL WHERE `serverId` = ?",
                        new String[]{ serverId });
            } catch (SQLException e_remove) { }
        }
        else
        {
            String dbString = convertRoleTemplatesToString(serverRoleTemplates);
            try {
                db.update("INSERT INTO `serverSettings` (`serverId`,`role_templates`) VALUES (?,?)",
                    new String[] { serverId, dbString});
            } catch (SQLException e) {
                //TODO: There is probably a much better way of doing this
                try {
                    db.update("UPDATE `serverSettings` SET `role_templates` = ? WHERE `serverId` = ?",
                            new String[] { dbString, serverId });
                } catch (SQLException e1) {
                    // Not much we can do if there is also an update error
                    log.error("Error writing server setting to database on updateRoleTemplatesDB.", e1);
                }
            }
        }
    }

    private static String convertRoleTemplatesToString(SortedMap<String, List<RaidRole>> roleTemplates) {
        String result = "";
        if (roleTemplates == null)
            return result;
        Iterator<String> templateNamesIt = roleTemplates.keySet().iterator();
        Iterator<List<RaidRole>> roleNamesIt = roleTemplates.values().iterator();
        for (int g = 0; g < roleTemplates.size(); g++)
        {
            result += templateNamesIt.next() + ";";
            List<RaidRole> roles = roleNamesIt.next();
            for (int r = 0; r < roles.size(); r++)
            {
                result += roles.get(r).getName() + ":" + Integer.toString(roles.get(r).getAmount());
                if (r < roles.size() - 1)
                    result += ",";
            }

            if (g < roleTemplates.size() - 1)
                result += roleTemplatesDelimiter;
        }
        return result;
    }

    private static SortedMap<String, List<RaidRole>> convertRoleTemplatesFromString(String input) {
        SortedMap<String, List<RaidRole>> result = new TreeMap<>();
        String[] templateSplits = input.split(roleTemplatesDelimiter);
        for (int t = 0; t < templateSplits.length; t++)
        {
            String[] nameSplits = templateSplits[t].split(";");
            if (nameSplits.length != 2)
                continue;
            String[] roleSplits = nameSplits[1].split(",");
            List<RaidRole> roleNames = new ArrayList<RaidRole>();
            for (int r = 0; r < roleSplits.length; r++)
            {
                String[] amountSplits = roleSplits[r].split(":"); // roles are saved as name:amount
                if (amountSplits.length != 2)
                    continue;
                roleNames.add(new RaidRole(Integer.parseInt(amountSplits[1]), amountSplits[0]));
            }
            result.put(nameSplits[0], roleNames);
        }
        return result;
    }

    /**
     * Loads the role templates for a server from the database if not already cached
     * @param serverId
     */
    private static void loadRoleTemplates(String serverId) {
        if (roleTemplates.get(serverId) != null)
            return;
        try {
            Database db = RaidBot.getInstance().getDatabase();
            QueryResult results = db.query("SELECT `role_templates` FROM `serverSettings` WHERE `serverId` = ?",
                    new String[]{serverId});
            if (results.getResults().next()) {
                String result = results.getResults().getString("role_templates");
                if (result != null) {
                    // construct sorted map
                    roleTemplates.put(serverId, convertRoleTemplatesFromString(result));
                }
            }
        } catch (Exception e) {
            log.error("Error trying to get role templates.", e);
        }
    }

    /**
     * Returns the set of role templates on this server
     * @param serverId
     * @return list of role templates
     */
    public static List<String> getRoleTemplateNames(String serverId) {
        loadRoleTemplates(serverId);
        SortedMap<String, List<RaidRole>> roleTemplatesServer = roleTemplates.get(serverId);
        List<String> result = new ArrayList<String>();
        if (roleTemplatesServer == null)
            return result;
        Iterator<String> it = roleTemplatesServer.keySet().iterator();
        while (it.hasNext())
            result.add(it.next());

        return result;
    }

    /**
     * Returns the list of raid roles corresponding to a template name
     * @param serverId
     * @param templateId
     * @return list of raid roles
     */
    public static List<RaidRole> getRolesForTemplate(String serverId, int templateId) {
        loadRoleTemplates(serverId);
        SortedMap<String, List<RaidRole>> roleTemplatesServer = roleTemplates.get(serverId);
        List<RaidRole> result = new ArrayList<RaidRole>();
        if (roleTemplatesServer == null)
            return result;
        Iterator<List<RaidRole>> it = roleTemplatesServer.values().iterator();
        try {
            for (int i = 0; i < templateId; i++)
                it.next();
            return it.next();
        } catch (Exception exc) {
            return result;
        }
    }

    /**
     * Returns a list of lists of raid roles corresponding to a all template names
     * @param serverId
     * @return list of lists of raid roles
     */
    public static List<List<RaidRole>> getAllRolesForTemplates(String serverId) {
        loadRoleTemplates(serverId);
        SortedMap<String, List<RaidRole>> roleTemplatesServer = roleTemplates.get(serverId);
        List<List<RaidRole>> result = new ArrayList<>();
        if (roleTemplatesServer == null)
            return result;
        Iterator<List<RaidRole>> it = roleTemplatesServer.values().iterator();
        while (it.hasNext())
            result.add(it.next());
        return result;
    }

    /**
     * Converts a template to a string for e.g. printing / displaying
     * @param template The template for which a string should be produced
     * @return The string representation of the template
     */
    public static String templateToString(String name, List<RaidRole> template) {
        String message = "";
        if (name != null)
            message += name + " (";
        for (int r = 0; r < template.size(); r ++) {
            message += template.get(r).getAmount() + " x " + template.get(r).getName();
            if (r != template.size() - 1) {
                message += ", ";
            }
        }
        if (name != null)
            message += ")";
        return message;
    }


    /*********************************/

    /**
     * checks if a given channel exists
     * @param serverId the id of the server to be checked
     * @param channelName the channel name
     * @return true if channel is valid, false otherwise
     */
    public static boolean checkChannel(String serverId, String channelName) {
        boolean validChannel = false;
        RaidBot bot = RaidBot.getInstance();
        for (TextChannel channel : bot.getServer(serverId).getTextChannels()) {
            if(channel.getName().replace("#","").equalsIgnoreCase(channelName)) {
                validChannel = true;
            }
        }
        return validChannel;
    }


    /**
     * checks if a given role exists
     * @param serverId the id of the server to be checked
     * @param roleName the role name
     * @return true if role is valid, false otherwise
     */
    public static boolean checkRole(String serverId, String roleName) {
        boolean validRole = false;
        RaidBot bot = RaidBot.getInstance();
        for (Role role : bot.getServer(serverId).getRoles()) {
            if(role.getName().equals(roleName)) {
                validRole = true;
                break;
            }
        }
        return validRole;
    }



    /**
     * checks if a valid archive channel is available for a server
     *
     * @param serverId
     * @return whether a valid archive channel is available
     */
    public static boolean isArchiveAvailable(String serverId) {
        return checkChannel(serverId, getArchiveChannel(serverId));
    }

}
