package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import me.cbitler.raidbot.raids.RaidRole;
import me.cbitler.raidbot.utility.RoleTemplates;

/**
 * Delete a role template
 * @author Franziska Mueller
 */
public class RoleTemplatesDeleteStep implements RoleTemplatesEditStep {

    private String serverId;
    private List<String> availableRoleTemplates;
    private List<List<RaidRole>> correspondingRoles;

    public RoleTemplatesDeleteStep(String serverId, List<String> availableRoleTemplates, List<List<RaidRole>> correspondingRoles) {
        this.serverId = serverId;
        this.availableRoleTemplates = availableRoleTemplates;
        this.correspondingRoles = correspondingRoles;
    }

    /**
     * Handle deleting a role template
     * @param e The direct message event
     * @return True if a role is deleted, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        int templateId = -1;

        // try to parse an integer
        try {
            templateId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
            if (templateId < 0 || templateId >= availableRoleTemplates.size())
                valid = false;
        } catch (Exception excp) {
            valid = false;
        }
        if (valid == false)
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
        else {
            ServerSettings.removeRoleTemplate(serverId, templateId);
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully deleted role template.").queue());
        }
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getStepText() {
        String header = "Which role group do you want to delete?";

        List<String> messages = RoleTemplates.buildListText(header, null, availableRoleTemplates, correspondingRoles, true);

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    public RoleTemplatesEditStep getNextStep() {
        return new RoleTemplatesIdleStep(serverId);
    }

    @Override
    public String getServerID() {
        return serverId;
    }
}
