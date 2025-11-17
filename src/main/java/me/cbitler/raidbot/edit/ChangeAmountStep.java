package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Change the amount for a role of the event
 * @author Franziska Mueller
 */
public class ChangeAmountStep implements EditStep {

    private String messageID;
    private int roleID;

    public ChangeAmountStep(String messageId) {
        this.messageID = messageId;
        this.roleID = -1;
    }

    public ChangeAmountStep(String messageId, int roleId) {
        this.messageID = messageId;
        this.roleID = roleId;
    }

    /**
     * Handle changing the amount for a role of the event
     * @param e The direct message event
     * @return True if a amount is changed, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean finished;

        int inputNumber;
        // try to parse an integer
        try {
            inputNumber = Integer.parseInt(e.getMessage().getContentRaw());
        } catch (Exception excp) {
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input. Try again.").queue());
            return false;
        }

        Raid raid = RaidManager.getRaid(messageID);
        List<RaidRole> roles = raid.getRoles();
        if (roleID == -1) // no role chosen yet
        {
            finished = false;
            inputNumber -= 1;
            if (inputNumber >= 0 && inputNumber < roles.size()) {
                roleID = inputNumber;
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(getStepText()).queue());
            } else
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input. Try again.").queue());
        }
        else // message contains new amount
        {
            if (inputNumber > 0) {
                finished = true; // we are done after we try to add
                int out = raid.changeAmountRole(roleID, inputNumber);
                if (out == 0) { // success
                    e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully changed amount.").queue());
                    raid.updateMessage();
                } else if (out == 1)
                    e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Amount could not be changed, number of users > new amount.").queue());
                else if (out == 2)
                    e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Amount could not be changed in database.").queue());
            } else {
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input, amount should be > 0. Try again.").queue());
                finished = false;
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
        if(roleID == -1) {
            stepText = "For which role do you want to change the amount? \n";
            for (int r = 0; r < roles.size(); r++){
                if(roles.get(r).isFlexOnly()) continue;
                stepText += "`" + (r+1) + "` " + roles.get(r).getName() + " \n";
            }
        } else {
            stepText = "Enter new amount for the role *" + roles.get(roleID).getName() + "*:";
        }
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
