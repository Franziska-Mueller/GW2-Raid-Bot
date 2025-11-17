package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the name for the event
 * @author Christopher Bitler
 */
public class RunNameStep implements CreationStep {

    String serverId;

    /**
     * Set the serverId for this step. This is needed for setting the serverId in the PendingRaid
     * @param serverId Server ID for the raid
     */
    public RunNameStep(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Set the name of the event and the server ID. Also create the event if it doesn't exist
     * @param e The direct message event
     * @return True always
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if(raid == null) {
            raid = new PendingRaid();
            raid.setLeaderId(e.getAuthor().getId());
            bot.getPendingRaids().put(e.getAuthor().getId(), raid);
        }

        raid.setName(e.getMessage().getContentRaw());
        raid.setServerId(serverId);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Event Setup:\nYou can type *cancel* at any point during this process to cancel the event setup\n\nEnter the name for the event:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunDescriptionStep();
    }
}
