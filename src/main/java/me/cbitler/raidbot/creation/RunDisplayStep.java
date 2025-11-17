package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * CURRENTLY UNUSED since almost all events are using long format
 * The display format can still be changed via `edit event` after creation
 * 
 * Step to set the display-short-message flag of the event
 * @author Franziska Mueller
 */
public class RunDisplayStep implements CreationStep {

    /**
     * Handle choosing a display format for the event
     * @param e The direct message event
     * @return True if the disaply-short-message flag was set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	boolean valid = true;
    	// try to parse an integer
    	int choiceID = 0;
    	try {
    		choiceID = Integer.parseInt(e.getMessage().getContentRaw());
    		if (choiceID < 1 || choiceID > 2)
    			valid = false;
    	} catch (Exception excp) {
    		valid = false;
    	}
    	if (valid == false) {
    		e.getChannel().sendMessage("Please choose a valid option.").queue();
    		return false;
    	}
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
        	// this will be caught in the handler
        	throw new RuntimeException();
        }
        raid.setDisplayShort(choiceID == 2);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Which format should the event message be displayed in?\n"
        		+ "`1` long\n"
        		+ "`2` short";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunDateStep();
    }
}
