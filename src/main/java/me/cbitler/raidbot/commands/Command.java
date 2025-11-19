package me.cbitler.raidbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public interface Command {
    void handleCommand(String command, String[] args, TextChannel channel, User author);
}
