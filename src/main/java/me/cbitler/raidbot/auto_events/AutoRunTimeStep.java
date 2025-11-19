package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the time for the event
 * @author Franziska Mueller
 */
public class AutoRunTimeStep implements AutoCreationStep {

	AutoPendingRaid event;

	public AutoRunTimeStep(AutoPendingRaid event) {
        this.event = event;
    }

    /**
     * Handle setting the time for the event
     * @param e The direct message event
     * @return True if the time is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        event.setTime(e.getMessage().getContentRaw());

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
    public AutoCreationStep getNextStep() {
        return new AutoRunOpenWorldStep(event);
    }

    /**
     * {@inheritDoc}
     */
	public AutoPendingRaid getEventTemplate() {
		return event;
	}
}
