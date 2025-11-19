package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the time for when the old event should be deleted daily
 * @author Franziska Mueller
 */
public class AutoRunDeleteTimeStep implements AutoCreationStep {

	AutoPendingRaid event;

	public AutoRunDeleteTimeStep(AutoPendingRaid event) {
        this.event = event;
    }

    /**
     * Handle setting the time for the event
     * @param e The direct message event
     * @return True if the time is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	String[] split = e.getMessage().getContentRaw().split(":");
    	try {
    		int hour = Integer.parseInt(split[0]);
    		int minutes = Integer.parseInt(split[1]);
    		if (hour < 0 || hour > 23 || minutes < 0 || minutes > 59)
    		{
    			e.getChannel().sendMessage("*Hour* has to be between 0 and 23, *minutes* has to be between 0 and 59.").queue();
    			return false;
    		}
    		else
    			event.setDeleteTime(hour, minutes);
    	} catch (Exception exp) {
            e.getChannel().sendMessage("Please use the correct format: hh:mm, e.g., 06:30.").queue();
            return false;
    	}

    	return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the time when the old event should be deleted [ format hh:mm ]. This can be the same as the re-post time:";
    }

    /**
     * {@inheritDoc}
     */
    public AutoCreationStep getNextStep() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
	public AutoPendingRaid getEventTemplate() {
		return event;
	}
}
