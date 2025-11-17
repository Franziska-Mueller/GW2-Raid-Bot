package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the time for the event
 * @author Christopher Bitler
 */
public class RunTimeStep implements CreationStep {

    /**
     * Handle setting the time for the event
     * @param e The direct message event
     * @return True if the time is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
        	// this will be caught in the handler
        	throw new RuntimeException();
        }

        raid.setTime(e.getMessage().getContentRaw());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the time for the event:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunOpenWorldStep();
    }
}
