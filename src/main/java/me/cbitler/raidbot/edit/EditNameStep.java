package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit the name for the event
 * @author Franziska Mueller
 */
public class EditNameStep implements EditStep {

	private String messageID;

	public EditNameStep(String messageId) {
		this.messageID = messageId;
	}

    /**
     * Handle changing the name for the event
     * @param e The direct message event
     * @return True if the name is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        Raid raid = RaidManager.getRaid(messageID);
        raid.setName(e.getMessage().getContentRaw());
        if (raid.updateNameDB()) {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Name successfully updated in database.").queue());
        } else {
        	e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Name could not be updated in database.").queue());
        }
        raid.updateMessage();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new name for the event:";
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
