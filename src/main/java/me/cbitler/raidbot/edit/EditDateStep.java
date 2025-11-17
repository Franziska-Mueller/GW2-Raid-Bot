package me.cbitler.raidbot.edit;

import me.cbitler.raidbot.raids.Raid;
import me.cbitler.raidbot.raids.RaidManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Edit the date for the event
 * @author Franziska Mueller
 */
public class EditDateStep implements EditStep {
    private static final Logger log = LogManager.getLogger(EditDateStep.class);

    private String messageID;

    public EditDateStep(String messageId) {
        this.messageID = messageId;
    }

    /**
     * Handle changing the date for the event
     * @param e The direct message event
     * @return True if the date is set, false otherwise
     */
    public boolean handleDM(MessageReceivedEvent e) {
        Raid raid = RaidManager.getRaid(messageID);
        raid.setDate(e.getMessage().getContentRaw());

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

        raid.setDate(dateString);

        if (raid.updateDateDB()) {
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Date successfully updated in database.").queue());
        } else {
            e.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Date could not be updated in database.").queue());
        }
        raid.updateMessage();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getStepText() {
        return "Enter the new date for the event, the weekday will be added automatically [ format dd.mm.yyyy ]:";
    }

    /**
     * {@inheritDoc}
     */
    public EditStep getNextStep() {
        return new EditIdleStep(messageID);
    }

    @Override
    public String getMessageID() {
        return messageID;
    }
}
