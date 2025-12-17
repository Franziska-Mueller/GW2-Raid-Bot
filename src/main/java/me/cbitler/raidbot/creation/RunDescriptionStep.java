package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the description for the event
 * @author Christopher Bitler
 */
public class RunDescriptionStep implements CreationStep {

    /**
     * Set the decsription for the event
     * @param e The direct message event
     * @return True always
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
        	// this will be caught in the handler
        	throw new RuntimeException();
        }

        raid.setDescription(e.getMessage().getContentRaw());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter description for the event:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunChannelStep();
    }
}
