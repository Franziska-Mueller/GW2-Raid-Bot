package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit the description for the event
 * @author Franziska Mueller
 */
public class EditDescriptionStep implements EditStep {

	private String messageID;

	public EditDescriptionStep(String messageId) {
		this.messageID = messageId;
	}

    /**
     * Handle changing the description for the event
     * @param e The direct message event
     * @return True if the description is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        Raid raid = RaidManager.getRaid(messageID);
        raid.setDescription(e.getMessage().getContentRaw());
        if (raid.updateDescriptionDB()) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Description successfully updated in database.").queue());
        } else {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Description could not be updated in database.").queue());
        }
        raid.updateMessage();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new description for the event:";
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
