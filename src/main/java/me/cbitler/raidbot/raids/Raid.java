package me.cbitler.raidbot.raids;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.database.Database;
import me.cbitler.raidbot.server_settings.ServerSettings;
import me.cbitler.raidbot.utility.PermissionsUtil;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a raid and has methods for adding/removing users, user flex roles,
 * etc
 */
public class Raid {
    private static final Logger log = LogManager.getLogger(Raid.class);

    String messageId, name, description, date, time, serverId, channelId, raidLeaderId;
    List<RaidRole> roles = new ArrayList<RaidRole>();
    HashMap<RaidUser, String> userToRole = new HashMap<RaidUser, String>();
    HashMap<RaidUser, List<FlexRole>> usersToFlexRoles = new HashMap<>();
    HashMap<String, String> userIDsToNicknames = new HashMap<>();
    List<String> permittedDiscordRoles = new ArrayList<String>();

    String flexRolesName = "Flex Roles / Backup";

    /* *
     * open world events only have a single role (Participants) and users sign up without any class
     */
    boolean isOpenWorld;

    /* *
     * whether to display the short version of the raid message
     */
    boolean isDisplayShort;

    /* *
     * whether the event is a fractal. Fractal events will not be archived and they can only be displayed as short message.
     */
    boolean isFractalEvent;

    /**
     * Create a new Raid with the specified data
     *
     * @param messageId      The embedded message Id related to this raid
     * @param serverId       The serverId that the raid is on
     * @param channelId      The announcement channel's id for this raid
     * @param raidLeaderId   The name of the raid leader
     * @param name           The name of the raid
     * @param date           The date of the raid
     * @param time           The time of the raid
     * @param isOpenWorld    Open world event flag
     * @param isDisplayShort Flag for short message
     * @param isFractalEvent Fractal event flag
     * @param permittedRoles List of discord roles allowed to sign up, empty list means everyone
     */
    public Raid(String messageId, String serverId, String channelId, String raidLeaderId, String name,
            String description, String date, String time, boolean isOpenWorld, boolean isDisplayShort,
            boolean isFractalEvent, List<String> permittedRoles) {
        this.messageId = messageId;
        this.serverId = serverId;
        this.channelId = channelId;
        this.raidLeaderId = raidLeaderId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.isOpenWorld = isOpenWorld;
        this.isDisplayShort = isDisplayShort;
        this.isFractalEvent = isFractalEvent;
        this.permittedDiscordRoles = permittedRoles;
        // add leadername to userIDsToNicknames
        // --- for compatibility with old format (where leadername was saved in stead of id) ---
        // check if provided leader id is a valid id and treat it as user name otherwise
        try {
            RaidBot.getInstance().getJda().getUserById(raidLeaderId);
        } catch (Exception excp) {
            if (setLeader(raidLeaderId) != 0) {
                // invalid user, set id to zero
                this.raidLeaderId = "";
            }
        }
        // -------------------------------------
        if (this.raidLeaderId.isEmpty())
            userIDsToNicknames.put(this.raidLeaderId, "unknown");
        else
            userIDsToNicknames.put(this.raidLeaderId, getNicknameOnServer(this.raidLeaderId, this.serverId));
    }

    /**
     * The open world flag for this event
     *
     * @return open world flag for this event
     */
    public boolean isOpenWorld() {
        return isOpenWorld;
    }

    /**
     * Get the message ID for this raid
     *
     * @return The message ID for this raid
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get the server ID for this raid
     *
     * @return The server ID for this raid
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Get the channel ID for this raid
     *
     * @return The channel ID for this raid
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * The display-short-message flag for this event
     *
     * @return display-short-message flag for this event
     */
    public boolean isDisplayShort() {
        return isDisplayShort;
    }

    /**
     * The fractal flag for this event
     *
     * @return fractal flag for this event
     */
    public boolean isFractalEvent() {
        return isFractalEvent;
    }

    /**
     * Sets the display-short-message flag for this event
     *
     * @param displayshort new display-short-message flag
     */
    public void setDisplayShort(boolean displayshort) {
        this.isDisplayShort = displayshort;
    }

    /**
     * Updates the display-short-message flag in the database
     */
    public boolean updateDisplayShortDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `isDisplayShort`=? WHERE `raidId`=?",
                    new String[] { Boolean.toString(isDisplayShort), messageId });
        } catch (SQLException e) {
            log.error("Error while updating isDisplayShorts for event {} in the database.", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Get the name of this raid
     *
     * @return The name of this raid
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the raid
     *
     * @param name The name of the raid
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Updates the name of the raid in the database
     */
    public boolean updateNameDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `name`=? WHERE `raidId`=?", new String[] { name, messageId });
        } catch (SQLException e) {
            log.error("Error updating name for raid {}.", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Set the leader of the raid
     *
     * @param leader The leader of the raid (either server nickname or discord name)
     * @return 0 if the provided new leader name is valid i.e. belongs to a unique existing user, 1 no user, 2 more than 1 user
     */
    public int setLeader(String leader) {
        if (leader.isEmpty())
            return 1;
        Guild server = RaidBot.getInstance().getServer(serverId);
        // first search by nickname
        List<Member> memberList = server.getMembersByNickname(leader, false);
        if (memberList.isEmpty()) {
            // search discord names
            memberList = server.getMembersByName(leader, false);
        }
        if (memberList.isEmpty())
            return 1;
        else if (memberList.size() == 1) {
            this.raidLeaderId = memberList.get(0).getUser().getId();
            userIDsToNicknames.put(this.raidLeaderId, getNicknameOnServer(this.raidLeaderId, this.serverId));
            return 0;
        } else { // more than 1
            return 2;
        }
    }

    /**
     * Updates the leader of the raid in the database
     */
    public boolean updateLeaderDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `leader`=? WHERE `raidId`=?", new String[] { raidLeaderId, messageId });
        } catch (SQLException e) {
            log.error("Failed to update leader id for event {}", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Get the description of the raid
     *
     * @return The description of the raid
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the raid
     *
     * @param description The description of the raid
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Updates the description of the raid in the database
     */
    public boolean updateDescriptionDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `description`=? WHERE `raidId`=?",
                    new String[] { description, messageId });
        } catch (SQLException e) {
            log.error("Error updating the description of event {}", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Get the date of this raid
     *
     * @return The date of this raid
     */
    public String getDate() {
        return date;
    }

    /**
     * Set the date of the raid
     *
     * @param date The date of the raid
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Updates the date of the raid in the database
     */
    public boolean updateDateDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `date`=? WHERE `raidId`=?",
                    new String[] { date, messageId });
        } catch (SQLException e) {
            log.error("Error updating date of event {}.", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Get the time of this raid
     *
     * @return The time of this raid
     */
    public String getTime() {
        return time;
    }

    /**
     * Set the time of the raid
     *
     * @param time The time of the raid
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Updates the time of the raid in the database
     */
    public boolean updateTimeDB() {
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `time`=? WHERE `raidId`=?",
                    new String[] { time, messageId });
        } catch (SQLException e) {
            log.error("Error updating time for event {}.", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Get the raid leader's id
     *
     * @return The raid leader's id
     */
    public String getRaidLeaderId() {
        return raidLeaderId;
    }

    /**
     * Get the raid leader's name
     *
     * @return The raid leader's name
     */
    public String getRaidLeaderName() {
        return userIDsToNicknames.get(raidLeaderId);
    }

    /**
     * Get the list of roles in this raid
     *
     * @return The list of roles in this raid
     */
    public List<RaidRole> getRoles() {
        return roles;
    }

    /**
     * Add a new role to the event
     * @param newrole new raid role
     * @return 0 success, 1 role exists, 2 SQL error
     */
    public int addRole(RaidRole newrole) {
        for (RaidRole role : roles) {
            if (role.getName().equalsIgnoreCase(newrole.getName())) {
                return 1;
            }
        }
        roles.add(newrole);

        String rolesString = RaidManager.formatRolesForDatabase(roles);
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `roles`=? WHERE `raidId`=?",
                    new String[] { rolesString, messageId });
            return 0;
        } catch (SQLException e) {
            log.error("Error updating roles for event {}.", messageId, e);
            return 2;
        }
    }


    /**
     * Rename a role of the event
     * @param id the role id
     * @param newname new name for the role
     * @return 0 success, 1 role exists, 2 SQL error
     */
    public int renameRole(int id, String newname) {
        for (RaidRole role : roles) {
            if (role.getName().equalsIgnoreCase(newname)) {
                return 1;
            }
        }
        String oldName = roles.get(id).getName();
        roles.get(id).setName(newname);

        // iterate over users' roles and rename
        for (Map.Entry<RaidUser, String> user : userToRole.entrySet()) {
            if (user.getValue().equals(oldName))
                user.setValue(newname);
        }
        for (Map.Entry<RaidUser, List<FlexRole>> flex : usersToFlexRoles.entrySet()) {
            for (FlexRole frole : flex.getValue()) {
                if (frole.getRole().equals(oldName))
                    frole.setRole(newname);
            }
        }

        // rename in database
        String rolesString = RaidManager.formatRolesForDatabase(roles);
        try {
            Database db = RaidBot.getInstance().getDatabase();
            db.update("UPDATE `raids` SET `roles`=? WHERE `raidId`=?",
                    new String[] { rolesString, messageId });
            db.update("UPDATE `raidUsers` SET `role`=? WHERE `role`=? AND `raidId`=?",
                    new String[] { newname, oldName, messageId });
            db.update("UPDATE `raidUsersFlexRoles` SET `role`=? WHERE `role`=? AND `raidId`=?",
                    new String[] { newname, oldName, messageId });

            return 0;
        } catch (SQLException e) {
            log.error("Error updating role name for role {} to {} on event {}.", oldName, newname, messageId, e);
            return 2;
        }
    }


    /**
     * Change amount for a role of the event
     * @param id the role id
     * @param newamount new amount for the role
     * @return 0 success, 1 number of users > new amount, 2 SQL error
     */
    public int changeAmountRole(int id, int newamount) {
        String roleName = roles.get(id).getName();
        int numberUsers = getUserNumberInRole(roleName);
        if (newamount < numberUsers)
            return 1;

        roles.get(id).setAmount(newamount);

        // rename in database
        String rolesString = RaidManager.formatRolesForDatabase(roles);
        try {
            Database db = RaidBot.getInstance().getDatabase();
            db.update("UPDATE `raids` SET `roles`=? WHERE `raidId`=?",
                    new String[] { rolesString, messageId });
            return 0;
        } catch (SQLException e) {
            log.error("Error updating the role amount for role {} to {} on event {}.", roleName, newamount, messageId, e);
            return 2;
        }
    }

    /**
     * Change flex only status of a role
     * @param id the role id
     * @param newStatus new amount for the role
     * @return 0 success, 1 number of users > 0 when enabling flexOnly, 2 SQL error
     */
    public int changeFlexOnlyRole(int id, boolean newStatus) {
        String roleName = roles.get(id).getName();
        int numberUsers = getUserNumberInRole(roleName);
        if (0 < numberUsers)
            return 1;

        roles.get(id).setFlexOnly(newStatus);

        // rename in database
        String rolesString = RaidManager.formatRolesForDatabase(roles);
        try {
            Database db = RaidBot.getInstance().getDatabase();
            db.update("UPDATE `raids` SET `roles`=? WHERE `raidId`=?",
                    new String[] { rolesString, messageId });
            return 0;
        } catch (SQLException e) {
            log.error("Error flex-only on role {} to {} for event {}.", roleName, newStatus, messageId, e);
            return 2;
        }
    }


    /**
     * Delete a role from the event
     * @param id the role id
     * @return 0 success, 1 number of users > 0, 2 SQL error
     */
    public int deleteRole(int id) {
        String roleName = roles.get(id).getName();
        int numberUsers = getUserNumberInRole(roleName);
        int numberUsersFlex = getUserNumberInFlexRole(roleName);

        if (numberUsers > 0 || numberUsersFlex > 0)
            return 1;

        roles.remove(id);

        // delete in database
        String rolesString = RaidManager.formatRolesForDatabase(roles);
        try {
            Database db = RaidBot.getInstance().getDatabase();
            db.update("UPDATE `raids` SET `roles`=? WHERE `raidId`=?",
                    new String[] { rolesString, messageId });
            return 0;
        } catch (SQLException e) {
            log.error("Error deleting role {} from event {}.", roleName, messageId, e);
            return 2;
        }
    }


    /**
     * Check if a specific role is valid, and whether or not it's full
     *
     * @param role The role to check
     * @return True if it is valid and not full, false otherwise
     */
    public boolean isValidNotFullRole(String role) {
        return this.isValidNotFullRole(role, false);
    }

    /**
     * Check if a specific role is valid, and whether or not it's full
     *
     * @param role The role to check
     * @return True if it is valid and not full, false otherwise
     */
    public boolean isValidNotFullRole(String role, boolean flex) {
        RaidRole r = getRole(role);

        if (r != null) {
            if(r.isFlexOnly() && !flex) return false;
            int max = r.getAmount();
            if (getUserNumberInRole(role) < max) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check to see if a role is valid
     *
     * @param role The role name
     * @return True if the role is valid, false otherwise
     */
    public boolean isValidRole(String role) {
        return getRole(role) != null;
    }

    /**
     * Get the object representing a role
     *
     * @param role The name of the role
     * @return The object representing the specified role
     */
    public RaidRole getRole(String role) {
        for (RaidRole r : roles) {
            if (r.getName().equalsIgnoreCase(role)) {
                return r;
            }
        }

        return null;
    }

    /**
     * Get the number of users in a role
     *
     * @param role The name of the role
     * @return The number of users in the role
     */
    private int getUserNumberInRole(String role) {
        int inRole = 0;
        for (Map.Entry<RaidUser, String> entry : userToRole.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(role))
                inRole += 1;
        }

        return inRole;
    }

    /**
     * Get the number of users in a flex role
     *
     * @param role The name of the role
     * @return The number of users in the role
     */
    private int getUserNumberInFlexRole(String role) {
        int inRole = 0;
        for (Map.Entry<RaidUser, List<FlexRole>> flex : usersToFlexRoles.entrySet()) {
            if (flex.getKey() != null) {
                for (FlexRole frole : flex.getValue()) {
                    if (frole.getRole().equalsIgnoreCase(role))
                        inRole += 1;
                }
            }
        }

        return inRole;
    }

    /**
     * Get list of users in a role
     *
     * @param role The name of the role
     * @return The users in the role
     */
    public List<RaidUser> getUsersInRole(String role) {
        List<RaidUser> users = new ArrayList<>();
        for (Map.Entry<RaidUser, String> entry : userToRole.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(role)) {
                users.add(entry.getKey());
            }
        }

        return users;
    }

    /**
     * Returns the nickname of the user on a server. If no nickname is set, it returns the username instead
     * @param userId the ID of the user
     * @param serverId the server ID
     * @return the user's server nickname
     */
    private String getNicknameOnServer(String userId, String serverId) {
        Member member = RaidBot.getInstance().getServer(serverId).getMemberById(userId);
        if (member != null) {
            String nickname = member.getNickname();
            if (nickname == null)
                nickname = member.getUser().getName();;
            // escape _ in the user names (this will lead to markdown formatting otherwise)
            nickname = nickname.replace("_", "\\_");
            return nickname;
        } else
            return null;
    }

    /**
     * Add a user to this raid. This first creates the user and attempts to insert
     * it into the database, if needed Then it adds them to list of raid users with
     * their role
     *
     * @param id        The id of the user
     * @param name      The name of the user
     * @param spec      The specialization they are playing
     * @param role      The role they will be playing in the raid
     * @param db_insert Whether or not the user should be inserted. This is false
     *                  when the roles are loaded from the database.
     * @return true if the user was added, false otherwise
     */
    public boolean addUser(String id, String name, String spec, String role, boolean db_insert,
            boolean update_message) {
        RaidUser user = new RaidUser(id, name, spec, role);

        if (db_insert) {
            try {
                RaidBot.getInstance().getDatabase()
                        .update("INSERT INTO `raidUsers` (`userId`, `username`, `spec`, `role`, `raidId`)"
                                + " VALUES (?,?,?,?,?)", new String[] { id, name, spec, role, this.messageId });
            } catch (SQLException e) {
                return false;
            }
        }

        userToRole.put(user, role);
        usersToFlexRoles.computeIfAbsent(new RaidUser(id, name, "", ""), k -> new ArrayList<FlexRole>());
        if (userIDsToNicknames.get(id) == null)
            userIDsToNicknames.put(id, getNicknameOnServer(id, serverId));

        if (update_message) {
            updateMessage();
        }
        return true;
    }

    /**
     * Add a user to a flex role in this raid. This first creates the user and
     * attempts to insert it into the database, if needed Then it adds them to list
     * of raid users' flex roles with their flex role
     *
     * @param id        The id of the user
     * @param name      The name of the user
     * @param spec      The specialization they are playing
     * @param role      The flex role they will be playing in the raid
     * @param db_insert Whether or not the user should be inserted. This is false
     *                  when the roles are loaded from the database.
     * @return true if the user was added, false otherwise
     */
    public boolean addUserFlexRole(String id, String name, String spec, String role, boolean db_insert,
            boolean update_message) {
        RaidUser user = new RaidUser(id, name, "", "");
        FlexRole frole = new FlexRole(spec, role);

        if (db_insert) {
            try {
                RaidBot.getInstance().getDatabase()
                        .update("INSERT INTO `raidUsersFlexRoles` (`userId`, `username`, `spec`, `role`, `raidId`)"
                                + " VALUES (?,?,?,?,?)", new String[] { id, name, spec, role, this.messageId });
            } catch (Exception e) {
                return false;
            }
        }

        if (usersToFlexRoles.get(user) == null) {
            usersToFlexRoles.put(user, new ArrayList<FlexRole>());
        }
        if (userIDsToNicknames.get(id) == null)
            userIDsToNicknames.put(id, getNicknameOnServer(id, serverId));

        usersToFlexRoles.get(user).add(frole);
        if (update_message) {
            updateMessage();
        }
        return true;
    }

    /**
     * Add a user to this open world event with the default role
     *
     * @param id        The id of the user
     * @param name      The name of the user
     * @return true if the user was added, false otherwise
     */
    public boolean addUserOpenWorld(String id, String name) {
        boolean success = false;

        String roleName = roles.get(0).getName();
        if (isValidNotFullRole(roleName)) // there is still space
            success = addUser(id, name, "", roleName, true, true);
        else
            success = addUserFlexRole(id, name, "", roleName, true, true);

        return success;
    }

    /**
     * Check if a specific user is in this raid (main roles)
     *
     * @param id The id of the user
     * @return True if they are in the raid, false otherwise
     */
    public boolean isUserInRaid(String id) {
        for (Map.Entry<RaidUser, String> entry : userToRole.entrySet()) {
            if (entry.getKey().getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a user from this raid. This also updates the database to remove them
     * from the raid and any flex roles they are in
     *
     * @param id The user's id
     */
    public boolean removeUser(String id) {
        boolean found = false;
        Iterator<Map.Entry<RaidUser, String>> users = userToRole.entrySet().iterator();
        while (users.hasNext()) {
            Map.Entry<RaidUser, String> user = users.next();
            if (user.getKey().getId().equalsIgnoreCase(id)) {
                users.remove();
                found = true;
            }
        }

        Iterator<Map.Entry<RaidUser, List<FlexRole>>> usersFlex = usersToFlexRoles.entrySet().iterator();
        while (usersFlex.hasNext()) {
            Map.Entry<RaidUser, List<FlexRole>> userFlex = usersFlex.next();
            if (userFlex.getKey().getId().equalsIgnoreCase(id)) {
                usersFlex.remove();
                found = true;
            }
        }

        try {
            RaidBot.getInstance().getDatabase().update("DELETE FROM `raidUsers` WHERE `userId` = ? AND `raidId` = ?",
                    new String[] { id, getMessageId() });
            RaidBot.getInstance().getDatabase().update(
                    "DELETE FROM `raidUsersFlexRoles` WHERE `userId` = ? and `raidId` = ?",
                    new String[] { id, getMessageId() });
        } catch (SQLException e) {
            log.error("Error while removing a user from roles and/or flex roles on event {}.", getMessageId(), e);
        }

        if (found)
            updateMessage();

        return found;
    }

    /**
     * Send the dps report log links to the players in this raid
     *
     * @param logLinks The list of links
     */
    public void messagePlayersWithLogLinks(List<String> logLinks) {
        String logLinkMessage = "ArcDPS reports from **" + this.getName() + "**:\n";
        for (String link : logLinks) {
            logLinkMessage += (link + "\n");
        }

        final String finalLogLinkMessage = logLinkMessage;
        for (RaidUser user : this.userToRole.keySet()) {
            RaidBot.getInstance().getServer(this.serverId).getMemberById(user.id).getUser().openPrivateChannel()
                    .queue(privateChannel -> privateChannel.sendMessage(finalLogLinkMessage).queue());
        }
    }

    /**
     * Update the embedded message for the raid
     */
    public void updateMessage() {
        MessageEmbed embed = (isFractalEvent || isDisplayShort) ? buildEmbedShort(true) : buildEmbed(true);
        try {
            RaidBot.getInstance().getServer(getServerId()).getTextChannelById(getChannelId())
                    .editMessageEmbedsById(getMessageId(), embed).queue();
        } catch (Exception ignored) {
        }
    }

    /**
     * Build the embedded message that shows the information about this raid
     *
     * @param provide_instr whether instructions should be provided
     * @return The embedded message representing this raid
     */
    private MessageEmbed buildEmbed(boolean provide_instr) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getName() + "\t ||ID: " + messageId + "||");
        builder.addField("Description:", getDescription(), false);
        builder.addBlankField(false);
        if (getRaidLeaderName() != null) {
            builder.addField("Leader: ", "**" + getRaidLeaderName() + "**", false);
        }
        builder.addBlankField(false);
        builder.addField("Date: ", getDate(), true);
        builder.addField("Time: ", getTime(), true);
        builder.addBlankField(false);
        builder.addField("Roles:", buildRolesText(), true);
        List<String> flexRolesText = buildFlexRolesText();
        String currentFlexText = "";
        String nextFieldName = flexRolesName + ":";
        for (int s = 0; s < flexRolesText.size(); s++) {
            if (currentFlexText.length() + flexRolesText.get(s).length() <= 1024) {
                currentFlexText += flexRolesText.get(s);
            } else {
                builder.addField(nextFieldName, currentFlexText, true);
                nextFieldName = "";
                currentFlexText = flexRolesText.get(s);
            }
        }
        if (currentFlexText.isEmpty() == false) {
            builder.addField(nextFieldName, currentFlexText, true);
        }
        if (provide_instr && this.isOpenWorld == false) {
            builder.addBlankField(false);
            builder.addField("How to sign up:",
                "- To choose a main role, click on the reaction of the class you want to play.\n"
                + "- To sign up as a flex role, click on the flex reaction (Fx).\n"
                + "- To remove one or all of your sign-ups, click the red X reaction.\n"
                + "- To swap your main and flex roles, click the swap reaction."
                , false);
        }

        return builder.build();
    }

//    /**
//     * Build the short embedded message that shows the information about this raid
//     *
//     * @return The short embedded message representing this raid
//     */
//    private MessageEmbed buildEmbedShort() {
//        EmbedBuilder builder = new EmbedBuilder();
//        builder.setTitle(getName() + " " + getDescription() + " [" + getDate() + " " + getTime() + "]\t"
//        		+ "||ID: " + messageId + "||");
//        List<String> textPerRole = buildTextPerRole();
//        for (int r = 0; r < textPerRole.size(); r++) {
//        	builder.addField("", textPerRole.get(r), true);
//        }
//        String flexText = buildFlexRolesTextShort();
//        if (flexText.isEmpty() == false)
//        	builder.addField("", "Flex Roles:" + flexText, true);
//
//        return builder.build();
//    }

    /**
     * Build the short embedded message that shows the information about this raid
     *
     * @param provide_instr whether instructions should be provided
     * @return The short embedded message representing this raid
     */
    private MessageEmbed buildEmbedShort(boolean provide_instr) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getName() + " - [" + getDate() + " " + getTime() + "]\t"
                + "||ID: " + messageId + "||");
        Set<String> usersInMain = new HashSet<String>();
        String rolesTxt = buildRolesTextShort(usersInMain);
        builder.addField("Roles:", rolesTxt, true);

        String flexText = buildFlexRolesTextShort(usersInMain);
        if (flexText.isEmpty() == false) {
            //builder.addBlankField(false);
            builder.addField(flexRolesName + ":", flexText, true);
        }

        return builder.build();
    }

    /**
     * collects a list of users for every role
     * @param excludeUsers user ids to not include in the result, may be null
     * @return the map containing a list of users for each role
     */
    private Map<String, List<RaidUser>> collectFlexUsersByRole(Set<String> excludeUsers) {
        // collect names and specializations for each role
        Map<String, List<RaidUser>> flexUsersByRole = new HashMap<String, List<RaidUser>>();
        for (int r = 0; r < roles.size(); r++) {
            flexUsersByRole.put(roles.get(r).getName(), new ArrayList<RaidUser>());
        }

        for (Map.Entry<RaidUser, List<FlexRole>> flex : usersToFlexRoles.entrySet()) {
            if (flex.getKey() != null) {
                if (excludeUsers == null || excludeUsers.contains(flex.getKey().getId()) == false) {
                    for (FlexRole frole : flex.getValue()) {
                        flexUsersByRole.get(frole.getRole()).add(new RaidUser(flex.getKey().id, flex.getKey().name, frole.spec, null));
                    }
                }
            }
        }
        return flexUsersByRole;
    }

    /**
     * Build the flex roles text, which includes a list of flex roles users are
     * playing and their specs
     *
     * @return The flex role text
     */
    private List<String> buildFlexRolesText() {
        List<String> textList = new ArrayList<String>();
        if (isOpenWorld) {
            String text = "";
            for (Map.Entry<RaidUser, List<FlexRole>> flex : usersToFlexRoles.entrySet()) {
                if (flex.getKey() != null && flex.getValue().isEmpty() == false) {
                    String username = userIDsToNicknames.get(flex.getKey().getId());
                    if (username == null)
                        username = flex.getKey().getName();
                    text += ("- " + username + "\n");
                }
            }
            if (text.isEmpty() == false) {
                textList.add(text);
            }
        } else {
            Map<String, List<RaidUser>> flexUsersByRole = collectFlexUsersByRole(null);
            for (int r = 0; r < roles.size(); r++) {
                String text = "";
                String roleName = roles.get(r).getName();
                
                for (RaidUser user : flexUsersByRole.get(roleName)) {
                    String username = userIDsToNicknames.get(user.getId());
                    if (username == null)
                        username = user.getName();
                    RichCustomEmoji userEmote = Reactions.getEmoteByName(user.getSpec());
                    if(userEmote == null)
                        text += ("- " + username + " (" + user.getSpec() + ")\n");
                    else
                        text += ("<:"+userEmote.getName()+":"+userEmote.getId()+"> " + username + "\n");
                }
                if (text.isEmpty() == false || roles.get(r).isFlexOnly()) {
                    text = roleName + ":\n" + text + "\n";
                    textList.add(text);
                }
            }
        }

        return textList;
    }

    /**
     * Build the short flex roles text, which includes a list of flex roles users are
     * playing and their specs
     *
     * @return The short flex role text
     */
    private String buildFlexRolesTextShort(Set<String> excludeUsers) {
        String text = "";
        if (isOpenWorld) {
            for (Map.Entry<RaidUser, List<FlexRole>> flex : usersToFlexRoles.entrySet()) {
                if (flex.getKey() != null && flex.getValue().isEmpty() == false) {
                    if (text.isEmpty() == false)
                        text += ", ";
                    String username = userIDsToNicknames.get(flex.getKey().getId());
                    if (username == null)
                        username = flex.getKey().getName();
                    text += username;
                }
            }
        } else {
            Map<String, List<RaidUser>> flexUsersByRole = collectFlexUsersByRole(excludeUsers);
            for (int r = 0; r < roles.size(); r++) {
                String roleName = roles.get(r).getName();
                List<RaidUser> usersPerRole = flexUsersByRole.get(roleName);
                if (usersPerRole.isEmpty() == false) {
                    text += ("[ **" + roleName + "**: ");

                    for (int u = 0; u < usersPerRole.size(); u++) {
                        RaidUser user = usersPerRole.get(u);
                        if (u != 0)
                            text += ", ";
                        String username = userIDsToNicknames.get(user.getId());
                        if (username == null)
                            username = user.getName();
                        text += username;
                    }
                    text += " ] ";
                }
            }
        }

        return text;
    }

    /**
     * Build the role text, which shows the roles users are playing in the raids
     *
     * @return The role text
     */
    private String buildRolesText() {
        String text = "";
        for (RaidRole role : roles) {
            if(role.isFlexOnly()) continue;
            List<RaidUser> raidUsersInRole = getUsersInRole(role.name);
            text += "**" + role.name;
            if (role.amount > 1) {
                text += " ( " + raidUsersInRole.size() + " / " + role.amount + " )";
            }
            text += ":**\n";
            for (RaidUser user : raidUsersInRole) {
                String username = userIDsToNicknames.get(user.getId());
                if (username == null)
                    username = user.getName();
                if (isOpenWorld) {
                    text += ("- " + username + "\n");
                } else {
                    RichCustomEmoji userEmote = Reactions.getEmoteByName(user.spec);
                    if(userEmote == null)
                        text += "   - " + username + " (" + user.spec + ")\n";
                    else
                        text += "   <:"+userEmote.getName()+":"+userEmote.getId()+"> " + username + "\n";
                }
            }
            text += "\n";
        }
        return text;
    }

    /**
     * Build the short role text, which shows the roles users are playing in the raids
     *
     * @return The short role text
     */
    private String buildRolesTextShort(Set<String> usersInMain) {
        String text = "";
        for (RaidRole role : roles) {
            if(role.isFlexOnly()) continue;
            List<RaidUser> raidUsersInRole = getUsersInRole(role.getName());
            if (isOpenWorld) {
                text += "**" + role.getName();
                if (role.getAmount() > 1)
                {
                    text += " ( " + raidUsersInRole.size() + " / " + role.getAmount() + ")";
                }
                text += ":**\n";

                for (RaidUser user : raidUsersInRole) {
                    String username = userIDsToNicknames.get(user.getId());
                    if (username == null)
                        username = user.getName();
                    text += ("- " + username + "\n");
                }
                text += "\n";
            } else {
                for (int s = 0; s < role.getAmount(); s++) {
                    text += "[ **" + role.getName() + "** ] ";
                    if (s < raidUsersInRole.size()) {
                        RaidUser user = raidUsersInRole.get(s);
                        if (usersInMain != null)
                            usersInMain.add(user.getId());
                        String username = userIDsToNicknames.get(user.getId());
                        if (username == null)
                            username = user.getName();

                        RichCustomEmoji userEmote = Reactions.getEmoteByName(user.getSpec());
                        if(userEmote == null)
                            text += username;
                        else
                            text += "<:"+userEmote.getName()+":"+userEmote.getId()+"> " + username;

                        // add flex roles for that user
                        List<FlexRole> userFlexRoles = usersToFlexRoles.get(new RaidUser(user.getId(), user.getName(), "", ""));
                        if (userFlexRoles.isEmpty() == false) {
                            text += "   (or ";
                            Set<String> uniqueFlexRoles = new HashSet<String>();
                            for (FlexRole frole : userFlexRoles) {
                                uniqueFlexRoles.add(frole.getRole());
                            }
                            text += uniqueFlexRoles.toString() + ")";
                        }
                    }
                    text += "\n";
                }
            }
        }
        text += "\n";
        return text;
    }

    /**
     * Get a List of RaidUsers from main roles in this raid by their ID
     *
     * @param id The user's ID
     * @return The List of RaidUsers if they are in this raid, null otherwise
     */
    public ArrayList<RaidUser> getRaidUsersById(String id) {
        ArrayList<RaidUser> raidUsers = new ArrayList<RaidUser>();
        for (RaidUser user : userToRole.keySet()) {
            if (user.getId().equalsIgnoreCase(id)) {
                raidUsers.add(user);
            }
        }
        return raidUsers;
    }

    /**
     * Get a List of RaidUsers from flex roles in this raid by their ID
     *
     * @param id The user's ID
     * @return The List of RaidUsers if they are in this raid, null otherwise
     */
    public ArrayList<FlexRole> getRaidUsersFlexRolesById(String id) {
        ArrayList<FlexRole> raidRoles = new ArrayList<FlexRole>();
        for (RaidUser user : usersToFlexRoles.keySet()) {
            if (user.getId().equalsIgnoreCase(id)) {
                for(FlexRole fRole : usersToFlexRoles.get(user)){
                    raidRoles.add(fRole);
                }
            }
        }
        return raidRoles;
    }

    /**
     * Remove a user by their username
     *
     * @param name The name of the user being removed
     */
    public void removeUserByName(String name) {
        String idToRemove = "";
        for (Map.Entry<RaidUser, String> entry : userToRole.entrySet()) {
            if (entry.getKey().name.equalsIgnoreCase(name)) {
                idToRemove = entry.getKey().id;
                break;
            }
        }
        if (idToRemove.isEmpty()) { // did not find the user in main roles, check flex roles
            for (Map.Entry<RaidUser, List<FlexRole>> entry : usersToFlexRoles.entrySet()) {
                if (entry.getKey().name.equalsIgnoreCase(name)) {
                    idToRemove = entry.getKey().id;
                    break;
                }
            }
        }

        removeUser(idToRemove);
    }


    /**
     * Remove a user from their main role
     *
     * @param id The id of the user being removed
     */
    public void removeUserFromMainRoles(String id, boolean update_message) {
        Iterator<Map.Entry<RaidUser, String>> users = userToRole.entrySet().iterator();
        while (users.hasNext()) {
            Map.Entry<RaidUser, String> user = users.next();
            if (user.getKey().getId().equalsIgnoreCase(id)) {
                users.remove();
            }
        }

        try {
            RaidBot.getInstance().getDatabase().update("DELETE FROM `raidUsers` WHERE `userId` = ? AND `raidId` = ?",
                    new String[] { id, getMessageId() });
        } catch (SQLException e) {
            log.error("Error while removing a user from main roles on event {}.", getMessageId(), e);
        }

        if (update_message)
            updateMessage();
    }


    /**
     * Remove a user from a flex role
     *
     * @param id The id of the user being removed
     * @param role The role that should be removed
     * @param spec The class specialization that should be removed
     * @return true if user was signed up for this role and class, false otherwise
     */
    public boolean removeUserFromFlexRoles(String id, String role, String spec, boolean update_message) {
        boolean found = false;
        Iterator<Map.Entry<RaidUser, List<FlexRole>>> users = usersToFlexRoles.entrySet().iterator();
        while (users.hasNext()) {
            Map.Entry<RaidUser, List<FlexRole>> user = users.next();
            if (user.getKey().getId().equalsIgnoreCase(id)) {
                Iterator<FlexRole> froles = user.getValue().iterator();
                while (froles.hasNext()) {
                    FlexRole frole = froles.next();
                    if (frole.getSpec().equals(spec) && frole.getRole().equals(role)) {
                        froles.remove();
                        found = true;
                    }
                }
            }
        }

        try {
            RaidBot.getInstance().getDatabase().update(
                    "DELETE FROM `raidUsersFlexRoles` WHERE `userId` = ? and `raidId` = ? and `role` = ? and `spec` = ?",
                    new String[] { id, getMessageId(), role, spec });
        } catch (SQLException e) {
            log.error("Error removing user from flex roles on event {}.", getMessageId(), e);
        }

        if (update_message)
            updateMessage();
        return found;
    }


    /**
     * Get the number of flex roles a user has
     *
     * @param id The id of the user
     * @return The number of flex roles that a user has
     */
    public int getUserNumFlexRoles(String id) {
        for (Map.Entry<RaidUser, List<FlexRole>> entry : usersToFlexRoles.entrySet()) {
            RaidUser user = entry.getKey();
            if (user != null && user.getId() != null) {
                if (user.id.equalsIgnoreCase(id)) {
                    return entry.getValue().size();
                }
            }
        }
        return 0;
    }

    /**
     * Posts the latest event message to the archive channel
     *
     * @return whether the message was posted successfully
     */
    public boolean postToArchive() {
        if (isFractalEvent) {
            // fractal events are not archived
            return false;
        }

        MessageEmbed message = isDisplayShort ? buildEmbedShort(false) : buildEmbed(false);

        Guild guild = RaidBot.getInstance().getServer(serverId);
        List<TextChannel> channels = guild.getTextChannelsByName(ServerSettings.getArchiveChannel(serverId), true);
        if(!channels.isEmpty()) {
            // We always go with the first channel if there is more than one
            try {
                channels.get(0).sendMessageEmbeds(message).queue();
            } catch (Exception ecxp) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public List<String> getPermittedDiscordRoles() {
        return permittedDiscordRoles;
    }

    public void addPermittedDiscordRoles(String role) {
        if (!permittedDiscordRoles.contains(role))
            permittedDiscordRoles.add(role);
    }

    public void addPermittedDiscordRoles(List<String> roles) {
        for (int r = 0; r < roles.size(); r++)
        {
            if (!permittedDiscordRoles.contains(roles.get(r)))
                permittedDiscordRoles.add(roles.get(r));
        }
    }

    public void clearPermittedDiscordRoles() {
        permittedDiscordRoles.clear();
    }

    public boolean updatePermDiscRolesDB() {
        String permDiscRoles = RaidManager.formatStringListForDatabase(permittedDiscordRoles);
        try {
            RaidBot.getInstance().getDatabase().update("UPDATE `raids` SET `permittedRoles`=? WHERE `raidId`=?", new String[] { permDiscRoles, messageId });
        } catch (SQLException e) {
            log.error("Error updating permitted roles for registration on event {}.", messageId, e);
            return false;
        }
        return true;
    }

    /**
     * Checks whether the given user is permitted to sign up for this event
     *
     * @param member the user as member
     * @return whether user has permission
     */
    public boolean isUserPermitted(Member member) {
        if (permittedDiscordRoles.isEmpty()) {
            // if there are no restrictions, user has permission
            return true;
        }
        boolean match = false;
        // iterate over permitted roles
        for (String permRole : permittedDiscordRoles) {
            // iterate over user discord roles
            if (PermissionsUtil.hasRole(member, permRole)) {
                match = true;
                break;
            }
        }
        return match;
    }
}
