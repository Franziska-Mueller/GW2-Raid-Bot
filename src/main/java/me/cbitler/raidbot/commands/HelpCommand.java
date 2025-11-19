package me.cbitler.raidbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class HelpCommand implements Command {
    private final String helpMessage = "**Riz-GW2-Event-Bot Help:**\n\n" +
            "__General commands:__\n\n" +
            "**!createEvent** - Start the event creation process. Usable by people with the event manager role.\n" +
            "**!editEvent [event id]** - Start the event edit process. Usable by people with the event manager role.\n" +
            "**!endEvent [event id] [log link 1] [log link 2] ...** - End an event and DM the users in the event with log links. The log links are optional arguments.\n" +
            "**!endAllEvents** - End all existing events. __Do not use this while any event is active__ since users will not be able to sign up for any existing event anymore! However, this should be done from time to time to free up memory.\n" +
            "**!removeFromEvent [event id] [name]** - Remove a player from an event. Only usable by people with the event manager role.\n" +
            "**!help** - You are looking at it.\n" +
            "**!info** - Information about the bot and it's authors.\n" +
            "\n_ _";

    private final String helpMessageServer =
            "__Server settings commands:__\n\n" +
            "**!setEventManagerRole [role]** - Set the role that serves as an event manager. This is only usable by people with the manage server permission.\n" +
            "**!setArchiveChannel [channel]** - Set the archive channel. This is only usable by people with the manage server permission.\n" +
            "**!editRoleGroups** - Start the edit process for role groups. Role groups make it easy to grant sign-up permissions to a set of discord roles. Usable by people with the event manager role.\n" +
            "**!editRoleTemplates** - Start the edit process for role templates. Role templates make it easy to save squad compositions for events. Usable by people with the event manager role.\n" +
            "\n_ _";

    private final String helpMessageFractals =
    		"__Fractal commands:__\n\n" +
    	            "**!createFractal [name];[date];[time];[team comp id] ** - Create a fractal event. Usable by people with the fractal creator role.\n" +
    	            "**!setFractalCreatorRole [role]** - Set the role that serves as fractal creator. This is only usable by people with the manage server permission.\n" +
    	            "**!setFractalChannel [channel]** - Set the fractal announcement channel. This is only usable by people with the manage server permission.\n" +
    	            "\n_ _";

    private final String helpMessageAutoEvents =
    		"__Auto event commands:__\n\n" +
    		"**!setAutoEventChannel [channel]** - Set the auto event announcement channel. This is only usable by people with the manage server permission.\n" +
    	            "**!startAutoEvent** - Setup a new event that repeats automatically on a daily basis. This is only usable by people with the manage server permission.\n" +
    	            "**!stopAutoEvent** - Stop one or multiple auto events currently active on this server. This is only usable by people with the manage server permission.\n" +
    	            "\n_ _";

    private final String helpInfo =
    		"__Help information:__\n\n" +
    	            "To use this bot, set the event manager role, and then anyone with that role can use !createEvent. This will take them through" +
    	            " an event setup process with the bot prompting them for information. After that, it will create the event in the channel specified." +
    	            " Once that is there, people can join it by clicking on the reaction for their class, and then responding to the bot with the role" +
    	            " that they want.";


    @Override
    public void handleCommand(String command, String[] args, TextChannel channel, User author) {
    	channel.sendMessage(helpMessage).queue();
    	channel.sendMessage(helpMessageServer).queue();
    	channel.sendMessage(helpMessageFractals).queue();
    	channel.sendMessage(helpMessageAutoEvents).queue();
    	channel.sendMessage(helpInfo).queue();
    }
}
