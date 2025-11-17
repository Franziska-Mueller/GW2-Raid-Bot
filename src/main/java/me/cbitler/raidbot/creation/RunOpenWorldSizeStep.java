package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidRole;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for choosing squad size for open world event.
 * @author Franziska Mueller
 */
public class RunOpenWorldSizeStep implements CreationStep {

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        try {
    		int size = Integer.parseInt(e.getMessage().getContentRaw());
    		RaidBot bot = RaidBot.getInstance();
            PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
            if (raid == null) {
            	// this will be caught in the handler
            	throw new RuntimeException();
            }
            raid.getRolesWithNumbers().add(new RaidRole(size, "Participants"));
        	return true;
    	} catch (Exception exp) {
            e.getChannel().sendMessage("Please choose a valid option.").queue();
            return false;
    	}
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the squad size for the open world event:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return null;
    }
}
