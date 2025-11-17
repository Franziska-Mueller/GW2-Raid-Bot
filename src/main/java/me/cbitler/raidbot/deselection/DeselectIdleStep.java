package me.cbitler.raidbot.deselection;

import me.cbitler.raidbot.raids.Raid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for removing a registration from a raid
 * @author Franziska Mueller
 */
public class DeselectIdleStep implements DeselectionStep {
    Raid raid;
    DeselectionStep nextStep;

    /**
     * Create a new step for role deselection with the specified raid
     * @param raid The raid
     */
    public DeselectIdleStep(Raid raid) {
        this.raid = raid;
    }

    /**
     * Handle the user input
     * @param e The private message event
     * @return True if the user chose a valid, not full, role, false otherwise
     */
    @Override
    public boolean handleDM(MessageReceivedEvent e) {
    	if (e.getMessage().getContentRaw().equalsIgnoreCase("done")) {
    		nextStep = null;
    		return true;
    	}

    	try {
    		int choiceId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;

    		if (choiceId == 0) { // main
        		// check if this user has a main role
        		if (raid.isUserInRaid(e.getAuthor().getId())) {
        			raid.removeUserFromMainRoles(e.getAuthor().getId(), true);
        			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Removed from main role. You can choose another type or write *done*.").queue());
        			return false;
        		}
        		else {
        			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not signed up for a main role. Choose a different type or write *done*.").queue());
                	return false;
        		}
        	} else if (choiceId == 1) { // flex
        		// check if this user has at least one flex role
        		if (raid.getUserNumFlexRoles(e.getAuthor().getId()) > 0) {
        			nextStep = new DeselectFlexRoleStep(raid,e.getAuthor());
        			return true;
        		}
        		else {
        			e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not signed up for any flex role. Choose a different type or write *done*.").queue());
                	return false;
        		}
        	} else if (choiceId == 2) { // all
        		nextStep = null;
        		raid.removeUser(e.getAuthor().getId());
        		return true;
        	} else { // some other integer
        		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again or type *done* to quit deselection.").queue());
        		return false;
        	}
    	} catch (Exception excp) {
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again or type *done* to quit deselection.").queue());
    		return false;
    	}
    }

    /**
     * Get the next step - no next step here as this is a one step process
     * @return null
     */
    @Override
    public DeselectionStep getNextStep() {
        return nextStep;
    }

    /**
     * The step text changes the text based on the available roles.
     * @return The step text
     */
    @Override
    public String getStepText() {
        return "Choose the role type you want to remove a sign-up from:\n"
        		+ "`1` main\n"
        		+ "`2` flex\n"
        		+ "`3` all\n"
        		+ "or write *done* to quit deselection.";
    }
}
