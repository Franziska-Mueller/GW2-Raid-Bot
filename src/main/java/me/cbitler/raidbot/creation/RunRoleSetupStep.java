package me.cbitler.raidbot.creation;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Role setup step for the event.
 * Gives a choice between role templates or manual role creation.
 * @author Franziska Mueller
 */
public class RunRoleSetupStep implements CreationStep {

	CreationStep nextStep;
	String serverId;
	
	public RunRoleSetupStep(String serverId) {
		this.serverId = serverId;
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
    			nextStep = new RunRoleSetupTemplateStep(serverId);
        		return true;
    		} else if (choiceId == 1) {
    			nextStep = new RunRoleSetupManualStep();
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
    public CreationStep getNextStep() {
        return nextStep;
    }
}
