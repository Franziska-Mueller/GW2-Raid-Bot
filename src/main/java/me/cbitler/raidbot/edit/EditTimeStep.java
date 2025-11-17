package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit the time for the event
 * @author Franziska Mueller
 */
public class EditTimeStep implements EditStep {

	private String messageID;

	public EditTimeStep(String messageId) {
		this.messageID = messageId;
	}

    /**
     * Handle changing the time for the event
     * @param e The direct message event
     * @return True if the time is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        Raid raid = RaidManager.getRaid(messageID);
        raid.setTime(e.getMessage().getContentRaw());
        if (raid.updateTimeDB()) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Time successfully updated in database.").queue());
        } else {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Time could not be updated in database.").queue());
        }
        raid.updateMessage();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new time for the event:";
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
