package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.util.Env;
import org.rundeck.client.util.Format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by greg on 5/20/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateInfo {
    public static final String ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public String date;
    public long unixtime;

    public DateInfo(final String date) {
        this.date = date;
    }

    public DateInfo() {
    }

    public Date toDate() throws ParseException {
        return toDate(ISO);
    }

    /**
     * Format using the default or configured date format
     * @return
     */
    public String format(String format) {
        try {
            return Format.date(toDate(), format);
        } catch (ParseException e) {
            return "?";
        }
    }

    public Date toDate(final String format) throws ParseException {
        SimpleDateFormat asdf = new SimpleDateFormat(format, Locale.US);
        return asdf.parse(date);
    }

    public String toRelative() throws ParseException {
        Date now = new Date();
        return toRelative(now);
    }

    public String toRelative(final Date time) throws ParseException {
        long diff = time.getTime() - toDate().getTime();
        return String.format("%dms ago", diff);
    }

    public static DateInfo withDate(Date input) {
        SimpleDateFormat asdf = new SimpleDateFormat(ISO, Locale.US);

        return new DateInfo(asdf.format(input));
    }

    @Override
    public String toString() {
        return "{" +
               date +
               ", unixtime=" + unixtime +
               '}';
    }
}
