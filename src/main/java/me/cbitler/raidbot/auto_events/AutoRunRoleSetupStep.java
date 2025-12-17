package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Role setup step for the event.
 * Gives a choice between role templates or manual role creation.
 * @author Franziska Mueller
 */
public class AutoRunRoleSetupStep implements AutoCreationStep {

	AutoCreationStep nextStep;
	AutoPendingRaid event;

	public AutoRunRoleSetupStep(AutoPendingRaid event) {
        this.event = event;
    }


    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user entered a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        try {
    		int choiceId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
    		if (choiceId == 0) { // role template
    			nextStep = new AutoRunRoleSetupTemplateStep(event);
        		return true;
    		} else if (choiceId == 1) {
    			nextStep = new AutoRunRoleSetupManualStep(event);
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
        return "Choose if you want to select a role template or enter them manually:\n"
        		+ "`1` template \n"
        		+ "`2` manual";
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
