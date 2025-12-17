package me.cbitler.raidbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class InfoCommand implements Command {
    private final String information = "Riz-GW2-Event-Bot Information:\n" +
            "Authors: J8-ET#1337, Raika-Sternensucher#6392\n" +
			"Based on GW2-Raid-Bot written by VoidWhisperer#5905\n";

    @Override
    public void handleCommand(String command, String[] args, TextChannel channel, User author) {
        channel.sendMessage(information).queue();
    }
}
