

package me.cbitler.raidbot.swap;

import me.cbitler.raidbot.raids.FlexRole;
import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Step for swapping main/flex roles in a raid
 * @author Franziska Mueller
 */
public class SwapStep {
    Raid raid;
    User user;
    boolean userHasMain;

    /**
     * Create a new step for role swapping with the specified raid
     * @param raid The raid
     */
    public SwapStep(Raid raid, User user) {
        this.raid = raid;
        this.user = user;
        this.userHasMain = raid.isUserInRaid(user.getId());
    }

    /**
     * Handle the user input
     * @param e The private message event
     * @return True if the user chose a valid role, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        ArrayList<FlexRole> rRoles = this.raid.getRaidUsersFlexRolesById(this.user.getId());
        String msg = e.getMessage().getContentRaw();
        int choiceId = -1;
        boolean valid = true;
        try {
    		choiceId = Integer.parseInt(msg) - 1;
    	} catch (Exception excp) { valid = false; }
        if(choiceId < 0 || choiceId >= (rRoles.size() + 1 + (userHasMain ? 1 : 0)))
			valid = false;

        if (valid == false) {
    		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
    	}

        if (userHasMain) {
        	if (choiceId == 0) {
        		SwapUtil.moveMainToFlex(raid, user.getId(), true);
        		return true;
        	} else
        		choiceId -= 1; // decrease by 1 to get the chosen flex role id
        }
        if (choiceId < rRoles.size()) {
        	if (SwapUtil.moveFlexToMain(raid, user, choiceId)) {
        		return true;
        	} else {
        		e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Your chosen role does not have a free spot in the main squad. Try again.").queue());
        		return false;
        	}
        } else {
        	// user chose cancel
        	return true;
        }
    }

    private String buildSelectionText(ArrayList<FlexRole> raidUsers){
        int counter = 0;
        String outer = "";
        if (userHasMain)
        	outer += "`"+(++counter)+"` move main role to flex\n";
        outer += "Choose a flex role to be converted to your main role:\n";
        for (FlexRole rUser : raidUsers) {
            Emote userEmote = Reactions.getEmoteByName(rUser.getSpec());
            if (userEmote!=null) {
                outer += "`"+(++counter)+"` <:"+userEmote.getName()+":"+userEmote.getId()+"> "+rUser.getSpec()+", "+rUser.getRole()+"\n";
            } else {
                outer += "`"+(++counter)+"`     "+rUser.getSpec()+", "+rUser.getRole()+"\n";
            }
        }
        outer += "or \n";
        outer += "`"+(++counter)+"` cancel";
        return "What do you want to do?\n"+outer+"\n";
    }

    /**
     * The step text changes the text based on the available roles.
     * @return The step text
     */
    public String getStepText() {
        ArrayList<FlexRole> raidUsers = this.raid.getRaidUsersFlexRolesById(this.user.getId());
        return buildSelectionText(raidUsers);
    }
}
