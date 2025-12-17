package me.cbitler.raidbot.auto_events;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.utility.AutomatedTaskExecutor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Stop step for auto events.
 * Gives the user the option to choose which auto event to stop
 * @author Franziska Mueller
 */
public class AutoStopStep {

    List<String> tasks = new ArrayList<>();
    String serverId;

    public AutoStopStep(String serverId) {
		List<AutomatedTaskExecutor> taskExecs = RaidBot.getInstance().getAutoTasks(serverId);
		for (int t = 0; t < taskExecs.size(); t++)
		{
			tasks.add(taskExecs.get(t).getName());
		}
		this.serverId = serverId;
	}

    /**
     * Handle user input.
     * @param e The direct message event
     * @return True if the user made a valid choice, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        boolean valid = true;
        ArrayList<Integer> choiceIds = new ArrayList<>();
    	try {
        	String[] splits = e.getMessage().getContentRaw().split(",");
        	for (int c = 0; c < splits.length; c++)
        	{
        		int id = Integer.parseInt(splits[c]) - 1;
        		if (id < 0 || id >= tasks.size())
        		{
        			valid = false;
        			break;
        		}
        		else
        			choiceIds.add(id);
        	}
        } catch (Exception exp) {
            valid = false;
        }

        if (valid == false)
        {
        	e.getChannel().sendMessage("Please choose a valid option.").queue();
            return false;
        }
        else
        {
        	// stop chosen tasks
        	RaidBot bot = RaidBot.getInstance();
        	for (int c  = 0; c < choiceIds.size(); c++)
        	{
        		bot.stopAutoEvent(serverId, choiceIds.get(c));
        	}
        	return true;
        }

    }

    public String getStepText() {
        String message = "Choose which auto event to stop (comma-separated list to choose multiple): \n";
        for (int i = 0; i < tasks.size(); i++) {
        	message += "`" + (i+1) + "` " + tasks.get(i) + "\n";
        }
        message += "or write *cancel* to cancel.\n";

        return message;
    }

    public AutoStopStep getNextStep() {
        return null;
    }
}
