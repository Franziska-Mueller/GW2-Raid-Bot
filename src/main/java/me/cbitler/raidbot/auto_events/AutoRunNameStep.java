package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the name for the event
 * @author Franziska Mueller
 */
public class AutoRunNameStep implements AutoCreationStep {

    AutoPendingRaid event;

    /**
     * Set the serverId for this step. This is needed for setting the serverId in the PendingRaid
     * @param serverId Server ID for the raid
     */
    public AutoRunNameStep(String serverId) {
        event = new AutoPendingRaid();
        event.setLeaderId("");
        event.setServerId(serverId);
        event.setDescription("-");
        event.setDisplayShort(true);
    }

    /**
     * Set the name of the event and the server ID. Also create the event if it doesn't exist
     * @param e The direct message event
     * @return True always
     */
    public boolean handleDM(MessageReceivedEvent e) {
        event.setName(e.getMessage().getContentRaw());

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
    public AutoCreationStep getNextStep() {
        return new AutoRunTimeStep(event);
    }

    /**
     * {@inheritDoc}
     */
	public AutoPendingRaid getEventTemplate() {
		return event;
	}
}
