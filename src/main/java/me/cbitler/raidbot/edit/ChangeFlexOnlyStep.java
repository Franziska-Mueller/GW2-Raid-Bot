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
public class ChangeFlexOnlyStep implements EditStep {

    private String messageID;
    private int roleID;

    public ChangeFlexOnlyStep(String messageId) {
        this.messageID = messageId;
        this.roleID = -1;
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
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Set the `flex only` status on role *" + roles.get(roleID).getName() + "* by entering `1` to enable and `0` to disable it:").queue());
            } else
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input. Try again.").queue());
        }
        else // message contains new status
        {
            if (inputNumber == 0 || inputNumber == 1) {
                finished = true; // we are done after we try to add
                boolean newStatus = false;
                if(inputNumber==1) newStatus = true;
                int out = raid.changeFlexOnlyRole(roleID, newStatus);
                if (out == 0) { // success
                    final String statusChange = newStatus ? "enabled" : "disabled";
                    e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully *" + statusChange + "* `flex only` status.").queue());
                    raid.updateMessage();
                } else if (out == 1)
                    e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Status could not be changed, there are users registered for this role (as main role).").queue());
            } else {
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid input, should be `0` or `1`. Try again.").queue());
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
        stepText = "For which role do you want to change the `flex only` flag? \n";
        for (int r = 0; r < roles.size(); r++)
            stepText += "`" + (r+1) + "` " + roles.get(r).getName() + " \n";

        return stepText;
    }

    /**
     * {@inheritDoc}
     */
    public EditStep getNextStep() {
        RaidRole role = RaidManager.getRaid(messageID).getRoles().get(roleID);
        if(!role.isFlexOnly() && role.getAmount()==0){
            return new ChangeAmountStep(messageID, roleID);
        }
        return new EditIdleStep(messageID);
    }

    @Override
    public String getMessageID() {
        return messageID;
    }
}
