package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for choosing between structued or open world event.
 * @author Franziska Mueller
 */
public class RunOpenWorldStep implements CreationStep {

	CreationStep nextStep;

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        try {
    		int choiceId = Integer.parseInt(e.getMessage().getContentRaw());
    		RaidBot bot = RaidBot.getInstance();
            PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
            if (raid == null) {
            	// this will be caught in the handler
            	throw new RuntimeException();
            }
    		if (choiceId == 1) { // open world
    			raid.setOpenWorld(true);
    			nextStep = new RunOpenWorldSizeStep();
        		return true;
    		} else if (choiceId == 2) { // structured
    			nextStep = new RunRoleSetupTemplateStep(raid.getServerId());
        		return true;
    		} else {
    			e.getChannel().sendMessage("Please choose a valid option.").queue();
                return false;
    		}
    	} catch (Exception exp) {
            e.getChannel().sendMessage("Please choose a valid option.").queue();
            return false;
    	}
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Choose if the event should be open world or structured:\n"
        		+ "`1` **open world:** 1 default role, yes/no sign-up for users\n"
        		+ "`2` **structured:** customizable role setup, class sign-up for users";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return nextStep;
    }
}
