package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 * Represents a step while editing role groups
 * @author Franziska Mueller
 */
public interface RoleGroupsEditStep {

    /**
     * Handle the direct message for this step in the process
     * @param e The direct message event
     * @return True if we are done with this step, false if not
     */
    boolean handleDM(PrivateMessageReceivedEvent e);

    /**
     * Get the next step. Should create a new object representing the next step and return it.
     * @return The object representing the next  step
     */
    RoleGroupsEditStep getNextStep();

    /**
     * Get the text to display to the user in relation to this step
     * @return The text to display to the user in relation to this step.
     */
    String getStepText();

    /**
     * Get the serverId of the role groups being edited.
     * @return the serverId of the role groups being edited
     */
    String getServerID();
}
