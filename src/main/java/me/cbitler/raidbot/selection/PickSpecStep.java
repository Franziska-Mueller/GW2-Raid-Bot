package me.cbitler.raidbot.selection;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.utility.ClassesSpecs;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Step for a user picking a specialization (or core class)
 */
public class PickSpecStep implements SelectionStep {
    Raid raid;
    User user;
    boolean forceFlex;
    String coreClass;
    SelectionStep nextStep;
    String[] allSpecs;

    /**
     * Create a new step for spec selection with the specified raid and core class
     * that the user chose
     * @param raid The raid
     * @param coreclass The core class that the user chose
     */
    public PickSpecStep(Raid raid, String coreclass, User user, boolean forceFlex) {
        this.raid = raid;
        this.user = user;
        this.forceFlex = forceFlex;
        this.coreClass = coreclass;
        this.nextStep = null;
        this.allSpecs = ClassesSpecs.getSpecsForCore(coreClass);
    }

    /**
     * Handle the user input - checks to see if the spec they are picking is valid
     * and if so, moving on to next step
     * @param e The private message event
     * @return True if the user chose a valid specialization, false otherwise
     */
    @Override
    public boolean handleDM(MessageReceivedEvent e) {
        try {
            int specId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
            String spec = allSpecs[specId];
            if (raid.getRoles().size() == 1) { // if there is only one role, skip PickRoleStep
                PickRoleStep autoRoleStep = new PickRoleStep(raid, spec, user, forceFlex);
                if (false == autoRoleStep.pickRole(e.getAuthor().getId(), e.getAuthor().getName(), raid.getRoles().get(0).getName())) {
                    e.getChannel().sendMessage("Since there is only one role, selection was cancelled automatically.").queue();
                }
            } else {
                nextStep = new PickRoleStep(raid, spec, user, forceFlex);
            }
            return true;
        } catch (Exception exp) {
            e.getChannel().sendMessage("Please choose a valid specialization.").queue();
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
        String text = "Pick a specialization:\n";
        for (int i = 0; i < allSpecs.length; i++) {
            RichCustomEmoji specEmote = Reactions.getEmoteByName(allSpecs[i]);
            if (specEmote != null) {
                text += "`" + (i+1) + "` <:" + specEmote.getName() + ":" + specEmote.getId() + "> " + allSpecs[i] + "\n";
            } else {
                text += "`" + (i+1) + "` " + allSpecs[i] + "\n";
            }
        }
        text += "or type *cancel* to cancel role selection.";

        return text;
    }
}
