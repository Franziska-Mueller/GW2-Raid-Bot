package me.cbitler.raidbot.server_settings;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

import me.cbitler.raidbot.raids.RaidRole;
import me.cbitler.raidbot.utility.RoleTemplates;

/**
 * Edit a role template for server
 * @author Franziska Mueller
 */
public class RoleTemplatesIdleStep implements RoleTemplatesEditStep {

    private String serverId;
    private RoleTemplatesEditStep nextStep;
    private List<String> availableRoleTemplates;
    private List<List<RaidRole>> correspondingRoles;

    public RoleTemplatesIdleStep(String serverId) {
        this.serverId = serverId;
        availableRoleTemplates = ServerSettings.getRoleTemplateNames(serverId);
        correspondingRoles = ServerSettings.getAllRolesForTemplates(serverId);
    }

    /**
     * Idle step in editing process
     * @param e The direct message event
     * @return True if the user passed the name of an editable property
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        if(e.getMessage().getContentRaw().equalsIgnoreCase("done")) {
            nextStep = null;
        } else {
            // try to parse an integer
            try {
                int choiceId = Integer.parseInt(e.getMessage().getContentRaw());
                if (choiceId == 1) // add: always possible
                    nextStep = new RoleTemplatesAddStep(serverId, availableRoleTemplates);
                else if (choiceId == 2) // remove: only if we have at least one
                {
                    if (availableRoleTemplates.isEmpty())
                    {
                        e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("This server does not have any role templates to remove.").queue());
                        valid = false;
                    }
                    else
                        nextStep = new RoleTemplatesDeleteStep(serverId, availableRoleTemplates, correspondingRoles);
                }
                else
                    valid = false;
            } catch (Exception excp) {
                valid = false;
            }
        }

        if (valid == false) {
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Invalid choice. Try again.").queue());
        }
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getStepText() {
        String header = "**Edit role templates:**\n"
                        + "NOTE: You can type *cancel* at any point during this process to stop editing.\n\n"
                        + "Currently available role templates on this server:";

        String footer = "Choose what you want to do:\n"
                        + "`1` add a role template \n"
                        + "`2` remove a role template \n"
                        + "or type *done* when you want to finish editing.";

        List<String> messages = RoleTemplates.buildListText(header, footer, availableRoleTemplates, correspondingRoles, false);

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    public RoleTemplatesEditStep getNextStep() {
        return nextStep;
    }

    @Override
    public String getServerID() {
        return serverId;
    }
}
