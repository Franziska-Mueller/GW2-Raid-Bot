package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Delete a role group
 * @author Franziska Mueller
 */
public class RoleGroupsDeleteStep implements RoleGroupsEditStep {

	private String serverId;
	private Set<String> availableRoleGroups;
	private List<List<String>> correspondingRoles;

	public RoleGroupsDeleteStep(String serverId, Set<String> availableRoleGroups, List<List<String>> correspondingRoles) {
		this.serverId = serverId;
		this.availableRoleGroups = availableRoleGroups;
		this.correspondingRoles = correspondingRoles;
	}

    /**
     * Handle deleting a role from the event
     * @param e The direct message event
     * @return True if a role is deleted, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	boolean valid = true;
    	int groupId = -1;

    	// try to parse an integer
    	try {
    		groupId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
    		if (groupId < 0 || groupId >= availableRoleGroups.size())
    			valid = false;
    	} catch (Exception excp) {
    		valid = false;
    	}
    	if (valid == false)
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
    	else {
    		ServerSettings.removeRoleGroup(serverId, groupId);
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully deleted role group.").queue());
    	}
    	return valid;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String message;
        message = "Which role group do you want to delete? \n";

        Iterator<String> groupsIt = availableRoleGroups.iterator();
        for (int g = 0; g < availableRoleGroups.size(); g++)
        {
        	message += "`"+(g+1)+"` "+groupsIt.next()+" [ ";
        	int numRolesInGroup = correspondingRoles.get(g).size();
        	for (int r = 0; r < numRolesInGroup; r++)
        	{
        		message += correspondingRoles.get(g).get(r);
        		if (r < numRolesInGroup - 1)
        			message += ", ";
        	}
        	message += " ]\n";
        }

        return message;
    }

    /**
     * {@inheritDoc}
     */
    public RoleGroupsEditStep getNextStep() {
        return new RoleGroupsIdleStep(serverId);
    }

    @Override
	public String getServerID() {
		return serverId;
	}
}
