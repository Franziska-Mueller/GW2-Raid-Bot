package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

/**
 * Add a role to an event
 * @author Franziska Mueller
 */
public class AddRoleStep implements EditStep {

	private String messageID;
	
	public AddRoleStep(String messageId) {
		this.messageID = messageId;
	}
	
    /**
     * Handle adding a role to the event
     * @param e The direct message event
     * @return True if a role is added, false otherwise
     */
    public boolean handleDM(PrivateMessageReceivedEvent e) {
    	boolean valid = true;	
    	String[] parts = e.getMessage().getRawContent().split(":");
        if(parts.length < 2) {
            valid = false;
        } else {
            try {
                int amnt = Integer.parseInt(parts[0]);
                String roleName = parts[1];
                Raid raid = RaidManager.getRaid(messageID);
                if(raid.addRole(new RaidRole(amnt, roleName))) {
                	e.getChannel().sendMessage("Role added.").queue();
                	raid.updateMessage();
                }
                else
                	e.getChannel().sendMessage("New role could not be added to database.").queue();
            } catch (Exception ex) {
                e.getChannel().sendMessage("Invalid input: Make sure it's in the format of [amount]:[role name], like 1:DPS").queue();
                valid = false;
            }
        }

    	if (valid == false) {
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input. Try again.").queue());
    	}
    		
    	return valid;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new role for the event (format: [amount]:[role name], e.g. 1:DPS).";
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