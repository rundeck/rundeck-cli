/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.util.Format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DateInfo {
    public static final String ISO1 = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String ISO2 = "yyyy-MM-dd'T'HH:mm:ssXX";
    public static final String ISO = "yyyy-MM-dd'T'HH:mm:ssX";
    public String date;
    public long unixtime;

    @JsonCreator
    public DateInfo(final String date) {

        this.date = date;
        try {
            unixtime = toDate().getTime();
        } catch (ParseException e) {
        }
    }

    public DateInfo() {
    }
    public Date toDate() throws ParseException {
        return toDate(ISO1, ISO2, ISO);
    }

    /**
     * @return Format using the default or configured date format
     */
    public String format(String format) {
        try {
            return Format.date(toDate(), format);
        } catch (ParseException e) {
            return "?";
        }
    }

    public Date toDate(final String... formats) throws ParseException {
        for (int i = 0; i < formats.length - 1; i++) {
            String format = formats[i];
            try {

                SimpleDateFormat asdf = new SimpleDateFormat(format, Locale.US);
                return asdf.parse(date);
            } catch (ParseException ignored) {
            }
        }
        SimpleDateFormat asdf = new SimpleDateFormat(formats[formats.length - 1], Locale.US);
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
