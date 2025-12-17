package me.cbitler.raidbot.utility;

import java.util.HashMap;

public class ClassesSpecs {
    /**
     * List of classes and their specializations
     */
    static String[][] classesSpecs = {
            {
                "Guardian",
                "Dragonhunter",
                "Firebrand",
                "Willbender",
                "Luminary"
            },
            {
                "Revenant",
                "Herald",
                "Renegade",
                "Vindicator",
                "Conduit"
            },
            {
                "Warrior",
                "Berserker",
                "Spellbreaker",
                "Bladesworn",
                "Paragon"
            },
            {
                "Engineer",
                "Scrapper",
                "Holosmith",
                "Mechanist",
                "Amalgam"
            },
            {
                "Ranger",
                "Druid",
                "Soulbeast",
                "Untamed",
                "Galeshot"
            },
            {
                "Thief",
                "Daredevil",
                "Deadeye",
                "Specter",
                "Antiquary"
            },
            {
                "Elementalist",
                "Weaver",
                "Tempest",
                "Catalyst",
                "Evoker"
            },
            {
                "Mesmer",
                "Chronomancer",
                "Mirage",
                "Virtuoso",
                "Troubadour"
            },
            {
                "Necromancer",
                "Reaper",
                "Scourge",
                "Harbinger",
                "Ritualist"
            }
    };

    static HashMap<String, Integer> coreClassIds = new HashMap<String, Integer>() {{
        put("Guardian", 0);
        put("Revenant", 1);
        put("Warrior", 2);
        put("Engineer", 3);
        put("Ranger", 4);
        put("Thief", 5);
        put("Elementalist", 6);
        put("Mesmer", 7);
        put("Necromancer", 8);
    }};


    /**
     * Get the array of available specializations for this core class
     *
     * @param coreclass The core class
     * @return The array of available specializations for this core class
     */
    public static String[] getSpecsForCore(String coreclass) {
        Integer classId = coreClassIds.get(coreclass);
        if (classId != null) {
            return classesSpecs[classId];
        }
        else {
            return new String[0];
        }
    }
}
