package me.cbitler.raidbot.creation;

import me.cbitler.raidbot.RaidBot;
import me.cbitler.raidbot.raids.PendingRaid;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Step to set the date of the event
 * @author Christopher Bitler
 */
public class RunDateStep implements CreationStep {
    private static final Logger log = LogManager.getLogger(RunDateStep.class);

    /**
     * Handle inputting the date for the event
     * @param e The direct message event
     * @return True if the date was set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        RaidBot bot = RaidBot.getInstance();
        PendingRaid raid = bot.getPendingRaids().get(e.getAuthor().getId());
        if (raid == null) {
            // this will be caught in the handler
            throw new RuntimeException();
        }

        boolean valid = true;
        String dateString = "";
        String[] split = e.getMessage().getContentRaw().split("\\.");
        if (split.length != 3)
        {
            valid = false;
        }
        else
        {
            try {
                int day = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]);
                int year = Integer.parseInt(split[2]);
                LocalDate date = LocalDate.of(year, month, day);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy");
                formatter = formatter.withLocale(Locale.ENGLISH);
                dateString = date.format(formatter);
            } catch (Exception exp) {
                log.debug("Trying to parse date in direct message failed for input '{}'.", e.getMessage().getContentRaw(), exp);
                valid = false;
            }
        }

        if (valid == false)
        {
            e.getChannel().sendMessage("Please use the correct format: dd.mm.yyyy, e.g., 29.02.2020").queue();
            return false;
        }
        else
        {
            raid.setDate(dateString);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the date for the event, the weekday will be added automatically [ format dd.mm.yyyy ]:";
    }

    /**
     * {@inheritDoc}
     */
    public CreationStep getNextStep() {
        return new RunTimeStep();
    }
}
