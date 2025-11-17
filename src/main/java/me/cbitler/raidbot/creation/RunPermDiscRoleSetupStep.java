package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.server_settings.ServerSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;
import java.util.Set;

/**
 * Permitted role setup step for the event.
 * Gives a choice between preset roles or manual setup
 * @author Franziska Mueller
 */
public class RunPermDiscRoleSetupStep implements CreationStep {

	String serverId;
	Set<String> predefRoleGroupNames;

	public RunPermDiscRoleSetupStep(String serverId) {
		this.serverId = serverId;
		predefRoleGroupNames = ServerSettings.getPredefGroupNames(serverId);
	}

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	PendingRaid raid = RaidBot.getInstance().getPendingRaids().get(e.getAuthor().getId());
        if (raid == null)
        	throw new RuntimeException();

    	String[] chosenRoles = e.getMessage().getContentRaw().split(",");
    	for (int role = 0; role < chosenRoles.length; role++) {
    		// try to parse an integer
    		try {
        		int choiceId = Integer.parseInt(chosenRoles[role]) - 1;
        		if (choiceId < predefRoleGroupNames.size()) { // preset role group
        			raid.addPermittedDiscordRoles(ServerSettings.getPredefGroupRoles(serverId, choiceId));
        		} else if (choiceId == predefRoleGroupNames.size()) { // everyone
        			raid.clearPermittedDiscordRoles(); // make event availabe to everyone
        			return true;
        		}
        	} catch (Exception exp) {
                // it's not an integer but a role name, add it
                raid.addPermittedDiscordRoles(chosenRoles[role]);
        	}
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
    	String message = "Choose who should be able to sign up for the event:\n";
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
    public CreationStep getNextStep() {
        return null;
    }
}
