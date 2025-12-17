package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        String[] parts = e.getMessage().getContentRaw().split(":");
        if(parts.length < 2) {
            valid = false;
        } else {
            try {
                int amnt = Integer.parseInt(parts[0]);
                String roleName = parts[1];
                Raid raid = RaidManager.getRaid(messageID);
                int out = raid.addRole(new RaidRole(amnt, roleName));
                if(out == 0) {
                    e.getChannel().sendMessage("Role added.").queue();
                    raid.updateMessage();
                } else if (out == 1) {
                    valid = false;
                    e.getChannel().sendMessage("A role with this name already exists. Choose a different name:").queue();
                }
                else if (out == 2)
                    e.getChannel().sendMessage("New role could not be added to database.").queue();
            } catch (Exception ex) {
                e.getChannel().sendMessage("Invalid input: Make sure it's in the format of `[amount]:[role name]`, like `1:DPS`.\nMake the role `flex only` by prepending its name with an exclamation mark (`!`) or by setting the amount to `0`.").queue();
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
        return "Enter the new role for the event (format: `[amount]:[role name]`, e.g. `1:DPS`).\nMake the role `flex only` by prepending its name with an exclamation mark (`!`) or by setting the amount to `0`.";
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
