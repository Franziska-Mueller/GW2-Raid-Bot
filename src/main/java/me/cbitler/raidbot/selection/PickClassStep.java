package me.cbitler.raidbot.selection;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for a user picking a class (will only be used when clicking the flex role icon)
 */
public class PickClassStep implements SelectionStep {
    Raid raid;
    User user;
    boolean forceFlex;
    SelectionStep nextStep;

    /**
     * Create a new step for core class selection with the specified raid
     * that the user chose
     * @param raid The raid
     */
    public PickClassStep(Raid raid, User user, boolean forceFlex) {
        this.raid = raid;
        this.user = user;
        this.nextStep = null;
        this.forceFlex = forceFlex;
    }

    /**
     * Handle the user input - checks to see if the core class they are picking is valid
     * and if so, moving on to next step
     * @param e The private message event
     * @return True if the user chose a valid core class, false otherwise
     */
    @Override
    public boolean handleDM(MessageReceivedEvent e) {
        try {
            int classId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
            String coreClass = Reactions.coreClasses[classId];
            nextStep = new PickSpecStep(raid, coreClass, user, forceFlex);
            return true;
        } catch (Exception exp) {
            e.getChannel().sendMessage("Please choose a valid core class.").queue();
            return false;
        }
    }

    /**
     * Get the next step - null here as this is a one-step process
     * @return null
     */
    @Override
    public SelectionStep getNextStep() {
        return this.nextStep;
    }

    /**
     * The step text changes the text based on the available roles.
     * @return The step text
     */
    @Override
    public String getStepText() {
        String text = "Pick a core class:\n";
        for (int i = 0; i < Reactions.coreClasses.length; i++) {
            Emote specEmote = Reactions.getEmoteByName(Reactions.coreClasses[i]);
            if (specEmote != null) {
                text += "`" + (i+1) + "` <:" + specEmote.getName() + ":" + specEmote.getId() + "> " + Reactions.coreClasses[i] + "\n";
            } else {
                text += "`" + (i+1) + "` " + Reactions.coreClasses[i] + "\n";
            }
        }
        text += "or type *cancel* to cancel role selection.";

        return text;
    }
}
