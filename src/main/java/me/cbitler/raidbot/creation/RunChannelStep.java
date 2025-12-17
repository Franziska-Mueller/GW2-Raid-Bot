package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import me.cbitler.raidbot.server_settings.ServerSettings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Get the announcement channel for the event from the user
 * @author Christopher Bitler
 * @author Franziska Mueller
 */
public class RunChannelStep implements CreationStep {
    static String[] defaultChannels = { "board-of-adventures", "banner-of-tactics", "gw2-raid-bot", "test" };
	boolean enterManually;

	public RunChannelStep() {
		this.enterManually = false;
	}

	/**
     * Set the announcement channel
     * @param e The direct message event
     * @return true if the announcement channel was set, false if it was not
     */
    public boolean handleDM(MessageReceivedEvent e) {
    	RaidBot bot = RaidBot.getInstance();
    	PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
    	if (raid == null) {
    		// this will be caught in the handler
        	throw new RuntimeException();
    	}
    	String serverId = raid.getServerId();
        if (enterManually) {
            String channelWithoutHash = e.getMessage().getContentRaw().replace("#","");
        	if (ServerSettings.checkChannel(serverId, channelWithoutHash)) {
        		raid.setAnnouncementChannel(channelWithoutHash);
        	} else {
				e.getChannel().sendMessage("Please choose a valid channel.").queue();
				return false;
			}
			return true;
        } else {
        	try {
        		int choiceId = Integer.parseInt(e.getMessage().getContentRaw()) - 1;
        		if (choiceId >= 0 && choiceId < defaultChannels.length) { // one of the default channels
        			if (ServerSettings.checkChannel(serverId, defaultChannels[choiceId])) {
                		raid.setAnnouncementChannel(defaultChannels[choiceId]);
                	} else {
        				e.getChannel().sendMessage("Please choose a valid channel.").queue();
        				return false;
        			}
        			return true;
        		} else if (choiceId == defaultChannels.length) { // user wants to enter name manually
        			enterManually = true;
        			e.getChannel().sendMessage("Enter the channel for event announcement:").queue();
        			return false;
        		} else { // no valid choice
        			e.getChannel().sendMessage("Please choose a valid option.").queue();
        			return false;
        		}
        	} catch (Exception excp) { // not an integer
        		e.getChannel().sendMessage("Please choose a valid option.").queue();
        		return false;
        	}
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        String text = "Choose the channel for event announcement:\n";
        for (int c = 0; c < defaultChannels.length; c++)
        	text += "`" + (c+1) + "` *" + defaultChannels[c] + "*\n";
        text += "`" + (defaultChannels.length+1) + "` enter name manually";
        return text;
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunDateStep();
    }
}
