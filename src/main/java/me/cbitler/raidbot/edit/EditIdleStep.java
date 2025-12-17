package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit a property for the event
 * @author Franziska Mueller
 */
public class EditIdleStep implements EditStep {

	private String messageID;
	private EditStep nextStep;
	private boolean isFractalEvent;

	public EditIdleStep(String messageId) {
		this.messageID = messageId;
		Raid raid = RaidManager.getRaid(messageId);
		this.isFractalEvent = raid.isFractalEvent();
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
        		if (choiceId == 1) // time
        			nextStep = new EditTimeStep(messageID);
        		else if (choiceId == 2) // date
        			nextStep = new EditDateStep(messageID);
        		else if (choiceId == 3) // name
        			nextStep = new EditNameStep(messageID);
        		else {
        			if (isFractalEvent == false) {
        				if (choiceId == 4) // description
        					nextStep = new EditDescriptionStep(messageID);
        				else if (choiceId == 5) // leader
        					nextStep = new EditLeaderStep(messageID);
        				else if (choiceId == 6) // roles
        					nextStep = new EditRoleStep(messageID);
        				else if (choiceId == 7) // display format
        					nextStep = new EditDisplayStep(messageID);
        				else if (choiceId == 8) // change permitted discord roles
        					nextStep = new EditPermDiscRoleStep(messageID);
        				else
        					valid = false;
        			} else
        				valid = false;
        		}
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
        String message = "NOTE: You can type *cancel* at any point during this process to stop editing.\n\n"
        		+ "Choose which property you want to change:\n"
        		+ "`1` time \n"
        		+ "`2` date \n"
        		+ "`3` name \n";
        if (isFractalEvent == false) {
        	message += "`4` description \n"
        		+ "`5` leader \n"
        		+ "`6` roles \n"
        		+ "`7` display format \n"
        		+ "`8` permitted discord roles for sign-up \n";
        }
        message += "or type *done* when you want to finish editing.";
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public EditStep getNextStep() {
        return nextStep;
    }

	@Override
	public String getMessageID() {
		return messageID;
	}
}
