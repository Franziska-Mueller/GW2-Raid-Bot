package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Rename a role for the event
 * @author Franziska Mueller
 */
public class RenameRoleStep implements EditStep {

	private String messageID;
	private int roleID;

	public RenameRoleStep(String messageId) {
		this.messageID = messageId;
		this.roleID = -1;
	}

    /**
     * Handle renaming an existing role
     * @param e The direct message event
     * @return True if role is renamed, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	boolean finished;
    	Raid raid = RaidManager.getRaid(messageID);
    	List<RaidRole> roles = raid.getRoles();
    	if (roleID == -1) { // no role chosen yet
    		finished = false;
    		boolean valid = true;
    		// try to parse an integer
    		try {
    			int choiceId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
    			if (choiceId >= 0 && choiceId < roles.size())
    				roleID = choiceId;
    			else
    				valid = false;
    		} catch (Exception excp) {
    			valid = false;
    		}
    		if (valid == false)
        		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
    		else
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Enter a new name for the role *" + roles.get(roleID).getName() + "*:").queue());
    	}
    	else { // message contains new name
    		int out = raid.renameRole(roleID, e.getMessage().getContentRaw());
    		if (out == 0) {
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully renamed role.").queue());
    			raid.updateMessage();
    			finished = true;
    		} else if (out == 1) {
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("A role with this name already exists. Choose a different new name:").queue());
    	    	finished = false;
    		} else {
    			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Role could not be updated in database.").queue());
    			finished = true;
    		}
    	}

    	return finished;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String stepText;
        List<RaidRole> roles = RaidManager.getRaid(messageID).getRoles();

        stepText = "Which role do you want to rename? \n";
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
