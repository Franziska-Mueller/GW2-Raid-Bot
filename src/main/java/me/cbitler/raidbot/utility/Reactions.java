package me.cbitler.raidbot.utility;

import me.cbitler.raidbot.RaidBot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reactions {
    /**
     * List of reactions representing classes
     */
    static String[] specs = {
            "Luminary",
            "Conduit",
            "Paragon",
            "Amalgam",
            "Galeshot",
            "Antiquary",
            "Evoker",
            "Troubadour",
            "Ritualist",
            "Willbender",
            "Vindicator",
            "Bladesworn",
            "Mechanist",
            "Untamed",
            "Specter",
            "Catalyst",
            "Virtuoso",
            "Harbinger",
            "Dragonhunter",
            "Firebrand",
            "Herald",
            "Renegade",
            "Berserker",
            "Spellbreaker",
            "Scrapper",
            "Holosmith",
            "Druid",
            "Soulbeast",
            "Daredevil",
            "Deadeye",
            "Weaver",
            "Tempest",
            "Chronomancer",
            "Mirage",
            "Reaper",
            "Scourge",
            "Guardian",
            "Revenant",
            "Warrior",
            "Engineer",
            "Ranger",
            "Thief",
            "Elementalist",
            "Mesmer",
            "Necromancer"
    };

    public static String[] coreClasses = {
            "Guardian",
            "Revenant",
            "Warrior",
            "Engineer",
            "Ranger",
            "Thief",
            "Elementalist",
            "Mesmer",
            "Necromancer"
    };

    static RichCustomEmoji[] reactions = {
            getEmojiFromEnvVar("EMOTE_LUMINARY"),
            getEmojiFromEnvVar("EMOTE_CONDUIT"),
            getEmojiFromEnvVar("EMOTE_PARAGON"),
            getEmojiFromEnvVar("EMOTE_AMALGAM"),
            getEmojiFromEnvVar("EMOTE_GALESHOT"),
            getEmojiFromEnvVar("EMOTE_ANTIQUARY"),
            getEmojiFromEnvVar("EMOTE_EVOKER"),
            getEmojiFromEnvVar("EMOTE_TROUBADOUR"),
            getEmojiFromEnvVar("EMOTE_RITUALIST"),
            getEmojiFromEnvVar("EMOTE_WILLBENDER"),
            getEmojiFromEnvVar("EMOTE_VINDICATOR"),
            getEmojiFromEnvVar("EMOTE_BLADESWORN"),
            getEmojiFromEnvVar("EMOTE_MECHANIST"),
            getEmojiFromEnvVar("EMOTE_UNTAMED"),
            getEmojiFromEnvVar("EMOTE_SPECTER"),
            getEmojiFromEnvVar("EMOTE_CATALYST"),
            getEmojiFromEnvVar("EMOTE_VIRTUOSO"),
            getEmojiFromEnvVar("EMOTE_HARBINGER"),
            getEmojiFromEnvVar("EMOTE_DRAGONHUNTER"),
            getEmojiFromEnvVar("EMOTE_FIREBRAND"),
            getEmojiFromEnvVar("EMOTE_HERALD"),
            getEmojiFromEnvVar("EMOTE_RENEGADE"),
            getEmojiFromEnvVar("EMOTE_BERSERKER"),
            getEmojiFromEnvVar("EMOTE_SPELLBREAKER"),
            getEmojiFromEnvVar("EMOTE_SCRAPPER"),
            getEmojiFromEnvVar("EMOTE_HOLOSMITH"),
            getEmojiFromEnvVar("EMOTE_DRUID"),
            getEmojiFromEnvVar("EMOTE_SOULBEAST"),
            getEmojiFromEnvVar("EMOTE_DAREDEVIL"),
            getEmojiFromEnvVar("EMOTE_DEADEYE"),
            getEmojiFromEnvVar("EMOTE_WEAVER"),
            getEmojiFromEnvVar("EMOTE_TEMPEST"),
            getEmojiFromEnvVar("EMOTE_CHRONOMANCER"),
            getEmojiFromEnvVar("EMOTE_MIRAGE"),
            getEmojiFromEnvVar("EMOTE_REAPER"),
            getEmojiFromEnvVar("EMOTE_SCOURGE"),
            getEmojiFromEnvVar("EMOTE_GUARDIAN"),
            getEmojiFromEnvVar("EMOTE_REVENANT"),
            getEmojiFromEnvVar("EMOTE_WARRIOR"),
            getEmojiFromEnvVar("EMOTE_ENGINEER"),
            getEmojiFromEnvVar("EMOTE_RANGER"),
            getEmojiFromEnvVar("EMOTE_THIEF"),
            getEmojiFromEnvVar("EMOTE_ELEMENTALIST"),
            getEmojiFromEnvVar("EMOTE_MESMER"),
            getEmojiFromEnvVar("EMOTE_NECROMANCER"),
            getEmojiFromEnvVar("EMOTE_FLEX"),
            getEmojiFromEnvVar("EMOTE_SWAP"),
            getEmojiFromEnvVar("EMOTE_CANCEL"),
            getEmojiFromEnvVar("EMOTE_EDIT")
    };

    static RichCustomEmoji[] reactionsCore = {
            getEmojiFromEnvVar("EMOTE_GUARDIAN"),
            getEmojiFromEnvVar("EMOTE_REVENANT"),
            getEmojiFromEnvVar("EMOTE_WARRIOR"),
            getEmojiFromEnvVar("EMOTE_ENGINEER"),
            getEmojiFromEnvVar("EMOTE_RANGER"),
            getEmojiFromEnvVar("EMOTE_THIEF"),
            getEmojiFromEnvVar("EMOTE_ELEMENTALIST"),
            getEmojiFromEnvVar("EMOTE_MESMER"),
            getEmojiFromEnvVar("EMOTE_NECROMANCER"),
            getEmojiFromEnvVar("EMOTE_FLEX"),
            getEmojiFromEnvVar("EMOTE_SWAP"),
            getEmojiFromEnvVar("EMOTE_CANCEL"),
            getEmojiFromEnvVar("EMOTE_EDIT")
    };

    static RichCustomEmoji[] reactionsOpenWorld = {
            getEmojiFromEnvVar("EMOTE_CHECK"),
            getEmojiFromEnvVar("EMOTE_CANCEL"),
            getEmojiFromEnvVar("EMOTE_EDIT")
    };

    /**
     * Get an emoji from it's emote ID via JDA
     *
     * @param varName The ID of the emoji
     * @return The emote object representing that emoji
     */
    private static RichCustomEmoji getEmojiFromEnvVar(String varName) {
        return getEmoji(EnvVariables.getValue(varName));
    }

    /**
     * Get an emoji from it's emote ID via JDA
     *
     * @param id The ID of the emoji
     * @return The emote object representing that emoji
     */
    private static RichCustomEmoji getEmoji(String id) {
        return RaidBot.getInstance().getJda().getEmojiById(id);
    }

    /**
     * Get the list of reaction names as a list
     *
     * @return The list of reactions as a list
     */
    public static List<String> getSpecs() {
        return new ArrayList<>(Arrays.asList(specs));
    }

    /**
     * Get the list of emote objects
     *
     * @return The emotes
     */
    public static List<RichCustomEmoji> getEmotes() {
        return new ArrayList<>(Arrays.asList(reactions));
    }

    /**
     * Get the list of core class emote objects
     *
     * @return The emotes
     */
    public static List<RichCustomEmoji> getCoreClassEmotes() {
        return new ArrayList<>(Arrays.asList(reactionsCore));
    }

    /**
     * Get the list of open world emote objects
     *
     * @return The emotes
     */
    public static List<RichCustomEmoji> getOpenWorldEmotes() {
        return new ArrayList<>(Arrays.asList(reactionsOpenWorld));
    }

    public static RichCustomEmoji getEmoteByName(String name) {
        for (RichCustomEmoji emote : reactions) {
            if (emote != null && emote.getName().equalsIgnoreCase(name)) {
                return emote;
            }
        }
        return null;
    }
}
