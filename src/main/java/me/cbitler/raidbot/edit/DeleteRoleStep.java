package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Delete a role from the event
 * @author Franziska Mueller
 */
public class DeleteRoleStep implements EditStep {

	private String messageID;

	public DeleteRoleStep(String messageId) {
		this.messageID = messageId;
	}

    /**
     * Handle deleting a role from the event
     * @param e The direct message event
     * @return True if a role is deleted, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	boolean valid = true;
    	Raid raid = RaidManager.getRaid(messageID);
    	List<RaidRole> roles = raid.getRoles();
    	int roleID = -1;

    	// try to parse an integer
    	try {
    		roleID = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
    		if (roleID < 0 || roleID >= roles.size())
    			valid = false;
    	} catch (Exception excp) {
    		valid = false;
    	}
    	if (valid == false)
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
    	else {
    		int out = raid.deleteRole(roleID);
    		if (out == 0) { // success
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully deleted role.").queue());
    			raid.updateMessage();
    		} else if (out == 1)
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Role could not be deleted because users already signed up for it.").queue());
    		else if (out == 2)
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Role could not be deleted from database.").queue());
    	}
    	return valid;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String stepText;
        List<RaidRole> roles = RaidManager.getRaid(messageID).getRoles();
        stepText = "Which role do you want to delete? \n";
        for (int r = 0; r < roles.size(); r++)
        	stepText += "`" + (r+1) + "` " + roles.get(r).getName() + " \n";

        return stepText;
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
