package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by greg on 5/20/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateInfo {
    public String date;
    public long unixtime;

    Date toDate() throws ParseException {
        SimpleDateFormat asdf = new SimpleDateFormat("2015-05-13T16:58:59Z");
        return asdf.parse(date);
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.DateInfo{" +
               "date='" + date + '\'' +
               ", unixtime=" + unixtime +
               '}';
    }
}
