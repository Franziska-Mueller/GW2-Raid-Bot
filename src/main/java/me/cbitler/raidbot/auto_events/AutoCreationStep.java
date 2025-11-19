package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.raids.AutoPendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents a step in the creation of an automatically repeated event
 * @author Franziska Mueller
 */
public interface AutoCreationStep {

    /**
     * Handle the direct message for this step in the process
     * @param e The direct message event
     * @return True if we are done with this step, false if not
     */
    boolean handleDM(MessageReceivedEvent e);

    /**
     * Get the next step. Should create a new object representing the next step and return it.
     * @return The object representing the next  step
     */
    AutoCreationStep getNextStep();

    /**
     * Get the text to display to the user in relation to this step
     * @return The text to display to the user in relation to this step.
     */
    String getStepText();

    /**
     * Get the event template
     * @return The event template
     */
    AutoPendingRaid getEventTemplate();
}
