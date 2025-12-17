package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit the leader for the event
 * @author Franziska Mueller
 */
public class EditLeaderStep implements EditStep {

	private String messageID;

	public EditLeaderStep(String messageId) {
		this.messageID = messageId;
	}

    /**
     * Handle changing the leader for the event
     * @param e The direct message event
     * @return True if the leader is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        Raid raid = RaidManager.getRaid(messageID);
        int res = raid.setLeader(e.getMessage().getContentRaw());
        if (res == 0) {
        	if (raid.updateLeaderDB()) {
        		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Leader successfully updated in database.").queue());
        	} else {
        		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Leader could not be updated in database.").queue());
        	}
        	raid.updateMessage();
        } else if (res == 1) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("No valid user found. Make sure to use the nickname or discord name of a guild member. Try again or type *cancel* to stop editing.").queue());
        	return false;
        } else if (res == 2) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("This nickname or discord name is not unique. Try again or type *cancel* to stop editing.").queue());
        	return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new leader for the event (preferably server nickname, discord name works as well):";
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
