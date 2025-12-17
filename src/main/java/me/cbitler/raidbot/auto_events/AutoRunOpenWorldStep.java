package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for choosing between structued or open world event.
 * @author Franziska Mueller
 */
public class AutoRunOpenWorldStep implements AutoCreationStep {

	AutoCreationStep nextStep;
	AutoPendingRaid event;

	public AutoRunOpenWorldStep(AutoPendingRaid event) {
        this.event = event;
    }

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        try {
    		int choiceId = Integer.parseInt(e.getMessage().getContentRaw());

    		if (choiceId == 1) { // open world
    			event.setOpenWorld(true);
    			nextStep = new AutoRunOpenWorldSizeStep(event);
        		return true;
    		} else if (choiceId == 2) { // structured
    			nextStep = new AutoRunRoleSetupStep(event);
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
    public AutoCreationStep getNextStep() {
        return nextStep;
    }

    /**
     * {@inheritDoc}
     */
	public AutoPendingRaid getEventTemplate() {
		return event;
	}
}
