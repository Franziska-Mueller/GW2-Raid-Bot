package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Set the time for when the event should be re-posted daily
 * @author Franziska Mueller
 */
public class AutoRunResetTimeStep implements AutoCreationStep {

	AutoPendingRaid event;

	public AutoRunResetTimeStep(AutoPendingRaid event) {
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
    			event.setResetTime(hour, minutes);
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
        return "Enter the time when the event should be reposted [ format hh:mm ]:";
    }

    /**
     * {@inheritDoc}
     */
    public AutoCreationStep getNextStep() {
        return new AutoRunDeleteTimeStep(event);
    }

    /**
     * {@inheritDoc}
     */
	public AutoPendingRaid getEventTemplate() {
		return event;
	}
}
