package me.cbitler.raidbot.handlers;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.deselection.DeselectIdleStep;
import me.cbitler.raidbot.deselection.DeselectionStep;
import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import me.cbitler.raidbot.selection.PickClassStep;
import me.cbitler.raidbot.selection.PickSpecStep;
import me.cbitler.raidbot.selection.SelectionStep;
import me.cbitler.raidbot.swap.SwapStep;
import me.cbitler.raidbot.swap.SwapUtil;
import me.cbitler.raidbot.utility.Reactions;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionHandler extends ListenerAdapter {
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if (!e.isFromGuild())
			return;

        Raid raid = RaidManager.getRaid(e.getMessageId());
        if(e.getUser().isBot()) {
            return;
        }
        if (raid != null) {
			EmojiUnion emote = e.getEmoji();
            RaidBot bot = RaidBot.getInstance();
            // edit reaction, this is the same whether isOpenWorld or not
            // active chat check is within RaidBot function (since it can also be triggered via the command,
            // no need to repeat here
            if(emote.getName().equalsIgnoreCase("Edit")) {
bot.startEditEvent(e.getMessageId(), e.getMember(), e.getUser(), raid.getRaidLeaderId());
            } else {
                // all other reactions have different functionality for open world and structured events
                if (!raid.isOpenWorld()) {
                    int actvId = bot.userHasActiveChat(e.getUser().getId());
                    if (actvId != 0) {
                        RaidBot.writeNotificationActiveChat(e.getUser(), actvId);
                        e.getReaction().removeReaction(e.getUser()).queue();
                        return;
                    }
                    if (!raid.isUserPermitted(e.getMember())) {
                        e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Sign-up for this event is currently only available for specific discord roles: " + raid.getPermittedDiscordRoles().toString()).queue());
                        e.getReaction().removeReaction(e.getUser()).queue();
                        return;
                    }
                    if (Reactions.getSpecs().contains(emote.getName()) || emote.getName().equalsIgnoreCase("Flex")) {
                        // check if the user is already selecting a role
                        if (bot.getRoleSelectionMap().get(e.getUser().getId()) == null) {
                            // check if the user can select a role, i.e. not main + (max_num-1) flex roles or max_num flex roles yet
                            int numFlexRoles = raid.getUserNumFlexRoles(e.getUser().getId());
                            int maxNumRoles = RaidManager.getMaxNumRoles();
                            if ((raid.isUserInRaid(e.getUser().getId()) && numFlexRoles >= (maxNumRoles-1)) || numFlexRoles >= maxNumRoles) {
                                e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You have selected the maximum number of roles. Press the X reaction to re-select your roles.").queue());
                            } else {
                                SelectionStep step;
                                if (emote.getName().equalsIgnoreCase("Flex")) {
                                    step = new PickClassStep(raid, e.getUser(), true);
                                } else {
                                    step = new PickSpecStep(raid, emote.getName(), e.getUser(), false);
                                }
                                bot.getRoleSelectionMap().put(e.getUser().getId(), step);
                                e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(step.getStepText()).queue());

                            }
                        } else {
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are already selecting a role.").queue());
                        }
                    } else if(emote.getName().equalsIgnoreCase("X_")) {
                        if (raid.isUserInRaid(e.getUser().getId()) || raid.getUserNumFlexRoles(e.getUser().getId()) > 0) {
                            DeselectionStep step = new DeselectIdleStep(raid);
                            RaidBot.getInstance().getRoleDeselectionMap().put(e.getUser().getId(), step);
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(step.getStepText()).queue());
                        } else {
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not signed up for this event.").queue());
                        }
                    } else if(emote.getName().equalsIgnoreCase("Swap")) {
                        // check if the user is signed up for at least one role
                        String userId = e.getUser().getId();
                        int numFlexRoles = raid.getUserNumFlexRoles(userId);
                        if (!raid.isUserInRaid(userId) && numFlexRoles == 0) {
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not signed up for this event.").queue());
                        } else if(raid.isUserInRaid(userId) && numFlexRoles == 0) {
                            // user only has a main role, make it flex
                            SwapUtil.moveMainToFlex(raid, userId, true);
                        } else if(!raid.isUserInRaid(userId) && numFlexRoles == 1) {
                            // user has exactly one flex roles, try to make it main (only possible if there are free spots)
                            if(!SwapUtil.moveFlexToMain(raid, e.getUser(), 0)) {
                                e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("There are no free spots to make your flex a main role.").queue());
                            }
                        } else {
                            // user has main and flex roles OR multiple flex roles (but not necessarily a main role)
                            // ask the user which role should be swapped:
                            // if they choose the main role (if existent), make it flex (always possible)
                            // if they choose a flex role, try to make it main (and move possible main role to flex)
                            SwapStep step = new SwapStep(raid, e.getUser());
                            RaidBot.getInstance().getRoleSwapMap().put(userId, step);
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(step.getStepText()).queue());
                        }
                    }
                } else { // open world event
                    if (emote.getName().equalsIgnoreCase("Check")) {
                        if (raid.isUserInRaid(e.getUser().getId()) || raid.getUserNumFlexRoles(e.getUser().getId()) > 0) { // already signed up
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are already signed up for this event.").queue());
                        } else {
                            if (raid.addUserOpenWorld(e.getUser().getId(), e.getUser().getName()))
                                e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully signed up for the event.").queue());
                            else
                                e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Sign-up failed.").queue());
                        }
                    } else if (emote.getName().equalsIgnoreCase("X_")) {
                        if (raid.removeUser(e.getUser().getId()))
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Successfully removed from event.").queue());
                        else
                            e.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You are not signed up for this event.").queue());
                    }
                }
            }
            e.getReaction().removeReaction(e.getUser()).queue();
        }
    }
}
