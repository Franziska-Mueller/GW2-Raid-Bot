package me.cbitler.raidbot;

import me.cbitler.raidbot.auto_events.AutoCreationStep;
import me.cbitler.raidbot.auto_events.AutoStopStep;
import me.cbitler.raidbot.commands.*;
import me.cbitler.raidbot.creation.CreationStep;
import me.cbitler.raidbot.database.Database;
import me.cbitler.raidbot.deselection.DeselectionStep;
import me.cbitler.raidbot.edit.EditIdleStep;
import me.cbitler.raidbot.edit.EditStep;
import me.cbitler.raidbot.handlers.ChannelMessageHandler;
import me.cbitler.raidbot.handlers.DMHandler;
import me.cbitler.raidbot.handlers.ReactionHandler;
import me.cbitler.raidbot.raids.AutoPendingRaid;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.selection.SelectionStep;
import me.cbitler.raidbot.server_settings.RoleGroupsEditStep;
import me.cbitler.raidbot.server_settings.RoleTemplatesEditStep;
import me.cbitler.raidbot.swap.SwapStep;
import me.cbitler.raidbot.utility.AutomatedTaskExecutor;
import me.cbitler.raidbot.utility.EventCreator;
import me.cbitler.raidbot.utility.PermissionsUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.*;

/**
 * Class representing the raid bot itself.
 * This stores the creation/roleSelection map data and also the list of pendingRaids
 * Additionally, it also stores the database in use by the bot and serves as a way
 * for other classes to access it.
 *
 * @author Christopher Bitler
 * @author Franziska Mueller
 */
public class RaidBot {
    private static RaidBot instance;
    private JDA jda;
    private int maxNumAutoEvents = 3;

    HashMap<String, CreationStep> creation = new HashMap<String, CreationStep>();
    HashMap<String, EditStep> edits = new HashMap<String, EditStep>();
    HashMap<String, PendingRaid> pendingRaids = new HashMap<String, PendingRaid>();
    HashMap<String, SelectionStep> roleSelection = new HashMap<String, SelectionStep>();
    HashMap<String, DeselectionStep> roleDeselection = new HashMap<String, DeselectionStep>();
    HashMap<String, SwapStep> roleSwap = new HashMap<String, SwapStep>();
    HashMap<String, AutoCreationStep> creationAuto = new HashMap<String, AutoCreationStep>();
    HashMap<String, AutoStopStep> stopAuto = new HashMap<String, AutoStopStep>();
    HashMap<String, RoleGroupsEditStep> editRoleGroups = new HashMap<String, RoleGroupsEditStep>();
    HashMap<String, RoleTemplatesEditStep> editRoleTemplates = new HashMap<String, RoleTemplatesEditStep>();

    Set<String> editList = new HashSet<String>();
    Set<String> editRoleGroupsList = new HashSet<String>();
    Set<String> editRoleTemplatesList = new HashSet<String>();

    Database db;

    HashMap<String, List<AutomatedTaskExecutor>> autoEventCreator = new HashMap<>();

    /**
     * Create a new instance of the raid bot with the specified JDA api
     *
     * @param token the token used to connect to the Discord API
     */
    public RaidBot(String token) throws LoginException, InterruptedException {
        instance = this;

        Collection<GatewayIntent> intents = GatewayIntent.getIntents(GatewayIntent.DIRECT_MESSAGES.getRawValue() +
                                                                     GatewayIntent.GUILD_MESSAGE_REACTIONS.getRawValue() +
                                                                     GatewayIntent.GUILD_MEMBERS.getRawValue() +
                                                                     GatewayIntent.GUILD_MESSAGES.getRawValue() +
                                                                     GatewayIntent.MESSAGE_CONTENT.getRawValue()
        );

        this.jda = JDABuilder.create(token, intents)
                            .addEventListeners(new DMHandler(this),
                                               new ChannelMessageHandler(),
                                               new ReactionHandler())
                            .build()
                            .awaitReady();
        db = new Database();
        db.connect();
        RaidManager.loadRaids();

        CommandRegistry.addCommand("help", new HelpCommand());
        CommandRegistry.addCommand("info", new InfoCommand());
        CommandRegistry.addCommand("endEvent", new EndRaidCommand());
        CommandRegistry.addCommand("endAllEvents", new EndAllCommand());
    }

    /**
     * Map of UserId -> creation step for people in the creation process
     * @return The map of UserId -> creation step for people in the creation process
     */
    public HashMap<String, CreationStep> getCreationMap() {
        return creation;
    }

    /**
     * Map of UserId -> edit step for raids in the edit process
     * @return The map of UserId -> edit step for raids in the edit process
     */
    public HashMap<String, EditStep> getEditMap() {
        return edits;
    }

    /**
     * Map of the UserId -> roleSelection step for people in the role selection process
     * @return The map of the UserId -> roleSelection step for people in the role selection process
     */
    public HashMap<String, SelectionStep> getRoleSelectionMap() {
        return roleSelection;
    }

    /**
     * Map of the UserId -> roleDeselection step for people in the role deselection process
     * @return The map of the UserId -> roleDeselection step for people in the role deselection process
     */
    public HashMap<String, DeselectionStep> getRoleDeselectionMap() {
        return roleDeselection;
    }

    /**
     * Map of the UserId -> roleSwap step for people in the role swapping process
     * @return The map of the UserId -> roleSwap step for people in the role swapping process
     */
    public HashMap<String, SwapStep> getRoleSwapMap() {
        return roleSwap;
    }

    /**
     * Map of UserId -> auto creation step for people in the creation process for auto events
     * @return The map of UserId -> auto creation step for people in the creation process
     */
    public HashMap<String, AutoCreationStep> getAutoCreationMap() {
        return creationAuto;
    }

    /**
     * Map of UserId -> auto stop step for people stopping auto events
     * @return The map of UserId -> auto stop step
     */
    public HashMap<String, AutoStopStep> getAutoStopMap() {
        return stopAuto;
    }

    /**
     * Map of UserId -> role groups edit step for people editing role groups
     * @return The map of UserId -> role groups edit step
     */
    public HashMap<String, RoleGroupsEditStep> getEditRoleGroupsMap() {
        return editRoleGroups;
    }

    /**
     * Map of UserId -> role templates edit step for people editing role templates
     * @return The map of UserId -> role templates edit step
     */
    public HashMap<String, RoleTemplatesEditStep> getEditRoleTemplatesMap() {
        return editRoleTemplates;
    }

    /**
     * Map of the UserId -> pendingRaid step for raids in the setup process
     * @return The map of UserId -> pendingRaid
     */
    public HashMap<String, PendingRaid> getPendingRaids() {
        return pendingRaids;
    }

    /**
     * List of messageIDs for raids in the edit process
     * @return List of messageIDs for raids in the edit process
     */
    public Set<String> getEditList() {
        return editList;
    }

    /**
     * List of serverIDs for servers in the edit role groups process
     * @return List of serverIDs for servers in the edit role groups process
     */
    public Set<String> getEditRoleGroupsList() {
        return editRoleGroupsList;
    }

    /**
     * List of serverIDs for servers in the edit role templates process
     * @return List of serverIDs for servers in the edit role templates process
     */
    public Set<String> getEditRoleTemplatesList() {
        return editRoleTemplatesList;
    }

    /**
     * Get the JDA server object related to the server ID
     * @param id The server ID
     * @return The server related to that that ID
     */
    public Guild getServer(String id) {
        return jda.getGuildById(id);
    }

    /**
     * Exposes the underlying library. This is mainly necessary for getting Emojis
     * @return The JDA library object
     */
    public JDA getJda() {
        return jda;
    }

    /**
     * Get the database that the bot is using
     * @return The database that the bot is using
     */
    public Database getDatabase() {
        return db;
    }


    /**
     * Get the current instance of the bot
     * @return The current instance of the bot.
     */
    public static RaidBot getInstance() {
        return instance;
    }

    /**
     * Writes a message to the user notifying them that they have an active chat already
     * @param user the user
     * @param actvId the type of active acticity
     */
    public static void writeNotificationActiveChat(User user, int actvId) {
        String actvName;
        if (actvId == 1) actvName = "role selection";
        else if (actvId == 2) actvName = "role deselection";
        else if (actvId == 3) actvName = "create event";
        else if (actvId == 4) actvName = "edit event";
        else if (actvId == 5) actvName = "swap role";
        else if (actvId == 6) actvName = "create auto event";
        else if (actvId == 7) actvName = "stop auto event";
        else if (actvId == 8) actvName = "edit role groups";
        else if (actvId == 9) actvName = "edit role templates";
        else actvName = "";

        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You already have an active chat with me (" + actvName + "). Finish it first!").queue());
    }

    /**
     * Determines whether the user has an active chat with the bot which is waiting for DM input
     * @param id the user's id
     * @return
     * 0: no active chat,
     * 1: role selection,
     * 2: role deselection,
     * 3: event creation,
     * 4: event edit,
     * 5: swap roles,
     * 6: create auto event
     * 7: stop auto event
     * 8: edit role groups
     * 9: edit role templates
     */
    public int userHasActiveChat(String id) {
        int actvId = 0;
        if (roleSelection.get(id) != null) actvId = 1;
        else if (roleDeselection.get(id) != null) actvId = 2;
        else if (creation.get(id) != null) actvId = 3;
        else if (edits.get(id) != null) actvId = 4;
        else if (roleSwap.get(id) != null) actvId = 5;
        else if (creationAuto.get(id) != null) actvId = 6;
        else if (stopAuto.get(id) != null) actvId = 7;
        else if (editRoleGroups.get(id) != null) actvId = 8;
        else if (editRoleTemplates.get(id) != null) actvId = 9;

        return actvId;
    }


    /**
     * returns the maximum allowed number of auto events per server
     *
     * @return maximum allowed number of auto events per server
     */
    public int getMaxNumAutoEvents() {
        return maxNumAutoEvents;
    }


    /**
     * returns the number of currently active auto events for a server
     *
     * @param serverId
     * @return the number of auto events currently active for this server
     */
    public int getNumAutoEvents(String serverId) {
        List<AutomatedTaskExecutor> tasks = autoEventCreator.get(serverId);
        if (tasks == null)
            return 0;
        else
            return tasks.size();
    }


    /**
     * creates an auto event
     *
     * @param event The event template for the auto event
     * @return whether the auto event was successfully created
     */
    public boolean createAutoEvent(AutoPendingRaid event) {
        // check if the maximum number of events is reached for this server
        String serverId = event.getServerId();
        List<AutomatedTaskExecutor> tasks = autoEventCreator.get(serverId);
        if (tasks != null && tasks.size() >= maxNumAutoEvents)
            return false;
        if (tasks == null)
            tasks = new ArrayList<AutomatedTaskExecutor>();
        AutomatedTaskExecutor taskExec = new AutomatedTaskExecutor(new EventCreator(event, tasks.size()));
        tasks.add(taskExec);
        autoEventCreator.put(serverId, tasks);
        taskExec.startExecution();

        return true;
    }


    /**
     * stops an auto event
     *
     * @param serverId
     * @param taskId
     */
    public void stopAutoEvent(String serverId, int taskId) {
        List<AutomatedTaskExecutor> tasks = autoEventCreator.get(serverId);
        if (tasks != null)
        {
            AutomatedTaskExecutor task = tasks.remove(taskId);
            task.stop();
        }
    }


    /**
     * returns the list of automated tasks for a server
     *
     * @param serverId
     * @return list of automated tasks for this server (empty list if there are none)
     */
    public List<AutomatedTaskExecutor> getAutoTasks(String serverId) {
        List<AutomatedTaskExecutor> result = autoEventCreator.get(serverId);
        if (result == null)
            result = new ArrayList<>();
        return result;
    }


    /**
     * initiates event editing if user has correct permissions
     * @param messageId message id of the event message
     * @param member object that holds information about the discord user within a server
     * @param user the discord user
     * @param leaderId leader id for the event that should be edited
     */
    public void startEditEvent(String messageId, Member member, User user, String leaderId) {
        // check permissions here since raid leader should also be able to edit
        if (PermissionsUtil.hasRaidLeaderRole(member) || user.getId().contentEquals(leaderId)) {
            // check if this user already has an active chat
            int actvId = userHasActiveChat(user.getId());
            if (actvId != 0) {
                RaidBot.writeNotificationActiveChat(user, actvId);
                return;
            }
            // check if the raid is being edited by someone else
            if (getEditList().contains(messageId)) {
                user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The selected event is already being edited.").queue());
            } else {
                // start editing process
                EditStep editIdleStep = new EditIdleStep(messageId);
                user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(editIdleStep.getStepText()).queue());
                getEditMap().put(user.getId(), editIdleStep);
                getEditList().add(messageId);
            }
        } else {
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You do not have permissions to edit this event. If you think this is wrong, please contact your friendly IT person <3").queue());
        }
    }
}
