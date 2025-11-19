package me.cbitler.raidbot.commands;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.utility.PermissionsUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EndRaidCommand implements Command {
    @Override
    public void handleCommand(String command, String[] args, TextChannel channel, User author) {
        Member member = channel.getGuild().getMember(author);
        if(PermissionsUtil.hasRaidLeaderRole(member)) {
            if(args.length >= 1) {
                String raidId = args[0];
                Raid raid = RaidManager.getRaid(raidId);
                if (raid != null && raid.getServerId().equalsIgnoreCase(channel.getGuild().getId())) {
                    //Get list of log messages and send them
                    if (args.length > 1) {
                        List<String> links = new ArrayList<>();
                        for (int i = 1; i < args.length; i++) {
                            links.add(args[i]);
                        }

                        raid.messagePlayersWithLogLinks(links);
                    }
                    // post message in archive if available
                    boolean delete_message = raid.postToArchive();

                    boolean deleted = RaidManager.deleteRaid(raidId, delete_message);

                    if (deleted) {
                        author.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Event ended.").queue());
                    } else {
                        author.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("An error occured while ending the event.").queue());
                    }
                } else {
                    author.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("This event doesn't exist on this server.").queue());
                }
            }
        }
    }
}
