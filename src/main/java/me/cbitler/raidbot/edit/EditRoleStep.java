package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Edit the roles for the event (rename, add, delete, change amount)
 * @author Franziska Mueller
 */
public class EditRoleStep implements EditStep {

    private String messageID;
    private EditStep nextStep;

    public EditRoleStep(String messageId) {
        this.messageID = messageId;
    }

    /**
     * Handle changing roles for the event
     * @param e The direct message event
     * @return True if a role is changed, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        Raid raid = RaidManager.getRaid(messageID);
        // try to parse an integer
        try {
            int choiceId = Integer.parseInt(e.getMessage().getContentRaw());
            if (choiceId == 1) { // add role
            	if (raid.isOpenWorld()) {
            		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("No roles can be added for open world events.").queue());
            		valid = false;
            	} else
            		nextStep = new AddRoleStep(messageID);
            }
            else if (choiceId == 2) { // remove role
            	if (raid.isOpenWorld()) {
            		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("No roles can be removed for open world events.").queue());
            		valid = false;
            	} else if (raid.getRoles().size() <= 1) {
            		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("The event has only one role, it cannot be removed.").queue());
            		valid = false;
            	} else
            		nextStep = new DeleteRoleStep(messageID);
            }
            else if (choiceId == 3) // rename role
                nextStep = new RenameRoleStep(messageID);
            else if (choiceId == 4) // change amount
                nextStep = new ChangeAmountStep(messageID);
            else if (choiceId == 5) { // change flexOnly
            	if (raid.isOpenWorld()) {
            		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Flex-only flags cannot be modified for open world events.").queue());
            		valid = false;
            	} else
            		nextStep = new ChangeFlexOnlyStep(messageID);
            }
            else
                valid = false;
        } catch (Exception excp) {
            valid = false;
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
        return "How do you want to change the roles?\n"
                + "`1` add role\n"
                + "`2` delete role\n"
                + "`3` rename role\n"
                + "`4` change amount of a role\n"
                + "`5` set `flex only` flag of a role";
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
