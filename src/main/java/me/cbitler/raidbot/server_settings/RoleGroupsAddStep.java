package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Add a role group
 * @author Franziska Mueller
 */
public class RoleGroupsAddStep implements RoleGroupsEditStep {

    private String serverId;
	private Set<String> availableRoleGroups;

    public RoleGroupsAddStep(String serverId, Set<String> availableRoleGroups) {
        this.serverId = serverId;
        this.availableRoleGroups = availableRoleGroups;
    }

    /**
     * Handle adding a role group
     * @param e The direct message event
     * @return True if a role is added, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;

        String[] parts = e.getMessage().getContentRaw().split(":");
        if(parts.length != 2)
        {
        	e.getChannel().sendMessage("Invalid input: Make sure it's in the correct format. Did you forget the colon (:)?").queue();
            return false;
        }

        String groupName = parts[0].trim();
        if (groupName.contains(";") || groupName.contains("/") || groupName.contains(","))
        {
        	e.getChannel().sendMessage("The group name contains invalid symbols. Choose a different name.").queue();
            return false;
        }
        if (availableRoleGroups.contains(groupName))
        {
        	e.getChannel().sendMessage("This server already has a role group with this name. Choose a different name or delete the existing group first.").queue();
            return false;
        }

        List<String> roleNames = new ArrayList<>();

        try {
           	String[] roles = parts[1].trim().split("\\s*,\\s*");
           	roleNames = Arrays.asList(roles);
            boolean success = ServerSettings.addRoleGroup(serverId, groupName, roleNames);
            if (success == false)
            {
            	e.getChannel().sendMessage("Invalid input: One or more of these discord roles do not exist on this server. Try again.").queue();
                valid = false;
            }
            else
            	e.getChannel().sendMessage("Added group `"+groupName+"` which corresponds to "+roleNames.toString()+".").queue();
        } catch (Exception ex) {
            e.getChannel().sendMessage("Invalid input: Could not parse the list of roles. Make sure it's in the correct format.").queue();
            valid = false;
        }

        return valid;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter a new role group for the server\n"
        		+ "__format:__ `[group name]:[role 1],[role 2],...,[role n]`, e.g., `Experts+: Expert Raider, Master Raider`\n"
        		+ "(Note: You cannot use colon `:`, semi-colon `;`, slash `/`, or comma `,` in your template name!)\n";
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
