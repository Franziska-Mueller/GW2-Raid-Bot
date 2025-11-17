package me.cbitler.raidbot.deselection;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step in the role selection process
 * @author Franziska Mueller
 */
public interface DeselectionStep
{
    /**
     * Handle a direct message sent in relation to this step
     * @param e The private message event
     * @return True if the step is done, false otherwise
     */
    boolean handleDM(MessageReceivedEvent e);

    /**
     * Get the object representing the next step in the role selection process
     * @return The object representing the next step, or null
     */
    DeselectionStep getNextStep();

    /**
     * Get the text to show the user about this step
     * @return The text to show the user about this step.
     */
    String getStepText();
}
