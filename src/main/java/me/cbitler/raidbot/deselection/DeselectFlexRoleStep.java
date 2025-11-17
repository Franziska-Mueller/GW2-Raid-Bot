

package me.cbitler.raidbot.deselection;

import me.cbitler.raidbot.raids.FlexRole;
import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

/**
 * Step for removing a registration from a raid
 * @author Franziska Mueller
 */
public class DeselectFlexRoleStep implements DeselectionStep {
    Raid raid;
    User user;
    DeselectionStep nextStep;

    /**
     * Create a new step for role deselection with the specified raid
     * @param raid The raid
     */
    public DeselectFlexRoleStep(Raid raid, User user) {
        this.raid = raid;
        this.user = user;
    }

    /**
     * Handle the user input
     * @param e The private message event
     * @return True if the user chose a valid, not full, role, false otherwise
     */
    @Override
    public boolean handleDM(MessageReceivedEvent e) {
        ArrayList<FlexRole> rRoles = this.raid.getRaidUsersFlexRolesById(this.user.getId());
        String msg = e.getMessage().getContentRaw();
        if(rRoles.size()==0){
            // no roles
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not registered for any flex roles.").queue());
            return true;
        } else if(msg.contentEquals("1")){
            // remove all
            ArrayList<String> removedRaidUsers = new ArrayList<String>();
            for (FlexRole rRole : rRoles) {
                if(raid.removeUserFromFlexRoles(e.getAuthor().getId(), rRole.getRole(), rRole.getSpec(), true)){
                    removedRaidUsers.add("\""+rRole.getSpec()+", "+rRole.getRole()+"\"");
                }
            }
            if(removedRaidUsers.size()>0){
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Selected roles have been removed:\n"+String.join("\n", removedRaidUsers)).queue());
                return true;
            }
        } else if(msg.contentEquals((rRoles.size()+2)+"")){
            // cancel
            return true;
        } else {
            // remove list
            String[] roles = msg.split(",");
            ArrayList<String> removedRaidUsers = new ArrayList<String>();
            for (String role : roles) {
            	try {
            		int roleSelector = Integer.parseInt(role) - 2;
            		if(roleSelector >= 0 && roleSelector < rRoles.size()){
            			FlexRole raidRole = rRoles.get(roleSelector);
            			if(raid.removeUserFromFlexRoles(e.getAuthor().getId(), raidRole.getRole(), raidRole.getSpec(), true)){
            				removedRaidUsers.add("\""+raidRole.getSpec()+", "+raidRole.getRole()+"\"");
            			}
            		}
            	} catch (Exception excp) { }
            }
            if(removedRaidUsers.size()>0){
                e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Selected flex roles have been removed:\n"+String.join("\n", removedRaidUsers)).queue());
                return true;
            }
        }
        // Send new message because there was no valid selection
        e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid selection.\n\n"+buildSelectionText(rRoles)).queue());
        return false;
    }

    /**
     * Get the next step - no next step here as this is a one step process
     * @return null
     */
    @Override
    public DeselectionStep getNextStep() {
        return nextStep;
    }

    private String buildSelectionText(ArrayList<FlexRole> raidUsers){
        int counter = 0;
        String outer = "";
        outer += "`"+(++counter)+"` all\n";
        for (FlexRole rUser : raidUsers) {
            Emote userEmote = Reactions.getEmoteByName(rUser.getSpec());
            if(userEmote!=null){
                outer += "`"+(++counter)+"` <:"+userEmote.getName()+":"+userEmote.getId()+"> "+rUser.getSpec()+", "+rUser.getRole()+"\n";
            }else{
                outer += "`"+(++counter)+"`     "+rUser.getSpec()+", "+rUser.getRole()+"\n";
            }
        }
        outer += "`"+(++counter)+"` cancel";
        return "Choose the specification you want to remove from sign-ups:\n"+outer+"\n"+
                        "alternatively to repeating this step, you may specify a comma-separated list (e.g. 2,3,4) that should not include `all` or `cancel`.";
    }
    /**
     * The step text changes the text based on the available roles.
     * @return The step text
     */
    @Override
    public String getStepText() {
        ArrayList<FlexRole> raidUsers = this.raid.getRaidUsersFlexRolesById(this.user.getId());
        if(raidUsers.size()==0){ return "You are not registered for any flex roles."; }
        return buildSelectionText(raidUsers);
    }
}
