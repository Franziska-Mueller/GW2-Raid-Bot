package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.raids.RaidRole;
import me.cbitler.raidbot.server_settings.ServerSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Role setup step for the event.
 * Gives the user the option to choose a role template or return to manual creation.
 * @author Franziska Mueller
 */
public class RunRoleSetupTemplateStep implements CreationStep {

    CreationStep nextStep;
    List<List<RaidRole>> templates;
    List<String> templateNames;

    public RunRoleSetupTemplateStep(String serverId) {
    	this.templates = ServerSettings.getAllRolesForTemplates(serverId);
		this.templateNames = ServerSettings.getRoleTemplateNames(serverId);
	}

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user made a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
        	// this will be caught in the handler
        	throw new RuntimeException();
        }
        
        nextStep = new RunPermDiscRoleSetupStep(raid.getServerId());

        try {
            int choiceId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
            if (choiceId == templateNames.size()) { // user chose to add roles manually
                nextStep = new RunRoleSetupManualStep();
                return true;
            } else {
            	raid.addTemplateRoles(templates.get(choiceId));
                return true;
            }
        } catch (Exception exp) {
            e.getChannel().sendMessage("Please choose a valid option.").queue();
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String message = "Choose from these available role templates or go back to manual role creation: \n";
        for (int i = 0; i < templateNames.size(); i++) {
        	message += "`" + (i+1) + "` " + ServerSettings.templateToString(templateNames.get(i), templates.get(i)) + "\n";
        }

        return message + "`" + (templateNames.size()+1) + "` add roles manually";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return nextStep;
    }
}
