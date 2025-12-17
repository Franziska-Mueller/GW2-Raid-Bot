package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.server_settings.ServerSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;
import java.util.Set;

/**
 * Permitted role setup step for the event.
 * Gives a choice between preset roles or manual setup
 * @author Franziska Mueller
 */
public class EditPermDiscRoleStep implements EditStep {

	String messageID;
	Raid raid;
	String serverId;
	Set<String> predefRoleGroupNames;

	public EditPermDiscRoleStep(String messageId) {
		this.messageID = messageId;
		this.raid = RaidManager.getRaid(messageID);
		this.serverId = raid.getServerId();
		predefRoleGroupNames = ServerSettings.getPredefGroupNames(serverId);
	}

	private String getPermissionString() {
		if (raid.getPermittedDiscordRoles().isEmpty())
        	return "*everyone*";
        else
        	return raid.getPermittedDiscordRoles().toString();
    }

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	String[] chosenRoles = e.getMessage().getContentRaw().split(",");
        for (int role = 0; role < chosenRoles.length; role++) {
        	// try to parse an integer
        	try {
           		int choiceId = Integer.parseInt(chosenRoles[role]) - 1;
           		if (choiceId < predefRoleGroupNames.size()) { // preset role
           			raid.addPermittedDiscordRoles(ServerSettings.getPredefGroupRoles(serverId, choiceId));
           		} else if (choiceId == predefRoleGroupNames.size()) { // everyone
           			raid.clearPermittedDiscordRoles(); // make event availabe to everyone
           			break;
           		}
           	} catch (Exception exp) {
                // it's not an integer but a role name, add it
                raid.addPermittedDiscordRoles(chosenRoles[role]);
           	}
        }
        if (raid.updatePermDiscRolesDB()) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Permitted roles successfully updated in database.").queue());
    	} else {
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Permitted roles could not be updated in database.").queue());
    	}

        String messageNewPerm = "Sign-up is now available for: " + getPermissionString();
        e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(messageNewPerm).queue());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
    	String message = "Sign-up is currently available for " + getPermissionString() + ".\n"
    			+ "Choose who should be added:\n";
    	Iterator<String> it = predefRoleGroupNames.iterator();
    	for (int i = 0; i < predefRoleGroupNames.size(); i++) {
        	message += "`" + (i+1) + "` " + it.next() + "\n";
        }
    	message += "`" + (predefRoleGroupNames.size() + 1) + "` *everyone* \n\n"
        		+ "You can also enter a comma-separated list of the choices above and other role names, for example *1,Fractalist*.";
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public EditStep getNextStep() {
        return new EditIdleStep(messageID);
    }

    @Override
	public String getMessageID() {
		return messageID;
	}
}
