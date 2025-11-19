package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Edit a role groups for server
 * @author Franziska Mueller
 */
public class RoleGroupsIdleStep implements RoleGroupsEditStep {

	private String serverId;
	private RoleGroupsEditStep nextStep;
	private Set<String> availableRoleGroups;
	private List<List<String>> correspondingRoles;

	public RoleGroupsIdleStep(String serverId) {
		this.serverId = serverId;
		availableRoleGroups = ServerSettings.getPredefGroupNames(serverId);
		correspondingRoles = ServerSettings.getAllPredefGroupRoles(serverId);
	}

    /**
     * Idle step in editing process
     * @param e The direct message event
     * @return True if the user passed the name of an editable property
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        if(e.getMessage().getContentRaw().equalsIgnoreCase("done")) {
            nextStep = null;
        } else {
        	// try to parse an integer
        	try {
        		int choiceId = Integer.parseInt(e.getMessage().getContentRaw());
        		if (choiceId == 1) // add: always possible
        			nextStep = new RoleGroupsAddStep(serverId, availableRoleGroups);
        		else if (choiceId == 2) // remove: only if we have at least one
        		{
        			if (availableRoleGroups.isEmpty())
        			{
        				e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("This server does not have any role groups to remove.").queue());
        				valid = false;
        			}
        			else
        				nextStep = new RoleGroupsDeleteStep(serverId, availableRoleGroups, correspondingRoles);
        		}
        		else
        			valid = false;
        	} catch (Exception excp) {
        		valid = false;
        	}
        }

    	if (valid == false) {
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
    	}
    	return valid;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String message = "**Edit discord role groups:**\n"
        		+ "NOTE: You can type *cancel* at any point during this process to stop editing.\n\n"
        		+ "Currently available discord role groups on this server:\n";
        Iterator<String> groupsIt = availableRoleGroups.iterator();

        for (int g = 0; g < availableRoleGroups.size(); g++)
        {
        	message += "`"+groupsIt.next()+"`: ";
        	int numRolesInGroup = correspondingRoles.get(g).size();
        	for (int r = 0; r < numRolesInGroup; r++)
        	{
        		message += correspondingRoles.get(g).get(r);
        		if (r < numRolesInGroup - 1)
        			message += ", ";
        	}
        	message += "\n";
        }

        message += "\nChoose what you want to do:\n"
        		+ "`1` add a role group \n"
        		+ "`2` remove a role group \n"
        		+ "or type *done* when you want to finish editing.";
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public RoleGroupsEditStep getNextStep() {
        return nextStep;
    }

    @Override
	public String getServerID() {
		return serverId;
	}
}
