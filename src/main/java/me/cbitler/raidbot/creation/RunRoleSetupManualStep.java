package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Role setup step for the event.
 * This one should take multiple inputs and as a result it doesn't finish until the user
 * types 'done'.
 * @author Christopher Bitler
 * @author Franziska Mueller
 */
public class RunRoleSetupManualStep implements CreationStep {

	String serverId;

    /**
     * Handle user input - should be in the format [number]:[role] unless it is 'done'.
     * @param e The direct message event
     * @return True if the user entered 'done', false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
        	// this will be caught in the handler
        	throw new RuntimeException();
        }
        serverId = raid.getServerId();

        if(e.getMessage().getContentRaw().equalsIgnoreCase("done")) {
            if(raid.getRolesWithNumbers().size() > 0) {
                return true;
            } else {
                e.getChannel().sendMessage("You must add at least one role.").queue();
                return false;
            }
        } else {
            String[] parts = e.getMessage().getContentRaw().split(":");
            if(parts.length < 2) {
                e.getChannel().sendMessage("You need to specify the role in the format `[amount]:[role name]`.\nMake the role `flex only` by prepending its name with an exclamation mark (`!`) or by setting the amount to `0`.").queue();
            } else {
                try {
                    int amnt = Integer.parseInt(parts[0]);
                    String roleName = parts[1];
                    if (raid.existsRole(roleName))
                        e.getChannel().sendMessage("A role with this name already exists.").queue();
                    else if (amnt < 0)
                        e.getChannel().sendMessage("Amount needs to be a positive number.").queue();
                    else {
                        raid.getRolesWithNumbers().add(new RaidRole(amnt, roleName));
                        e.getChannel().sendMessage("Role added.").queue();
                    }
                } catch (Exception ex) {
                    e.getChannel().sendMessage("Invalid input: Make sure it's in the format of `[amount]:[role name]`, like `1:DPS`.").queue();
                }
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the roles for the event (format: [amount]:[role name], e.g. 1:DPS).\nMake the role `flex only` by prepending its name with an exclamation mark (`!`) or by setting the amount to `0`.\nType *done* to finish entering roles:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunPermDiscRoleSetupStep(serverId);
    }
}
