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
import org.rundeck.client.util.DataOutput;
import org.rundeck.client.util.Format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DateInfo
        implements DataOutput
{
    public static final String ISO1 = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String ISO2 = "yyyy-MM-dd'T'HH:mm:ssXX";
    public static final String ISO3 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
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
        return toDate(ISO1, ISO2, ISO3, ISO);
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
        String diffStr;
        String ago = "ago";
        String fmt = "%s %s";
        if (diff < 0) {
            ago = "from now";
            fmt = "(%s %s)";
        }
        if (diff == 0) {
            diffStr = "now";
            ago = "";
            fmt = "%s";
        } else if (diff < 1000) {
            diffStr = String.format("%dms", diff);
        } else if (diff < 1000 * 60) {
            diffStr = String.format("%ds", diff / 1000);
        } else if (diff < 1000 * 60 * 60) {
            diffStr = String.format("%dm", diff / 60000);
        } else if (diff < 1000 * 60 * 60 * 24) {
            diffStr = String.format("%dh", diff / (3600000));
        } else if (diff < 1000L * 60 * 60 * 24 * 31) {
            diffStr = String.format("%dd", diff / (24 * 3600000));
        } else {
            diffStr = String.format("%dms", diff);
        }
        return String.format(fmt, diffStr, ago);
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

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("unixtime", unixtime);
        map.put("date", date);
        try {
            map.put("relative", toRelative());
        } catch (ParseException ignored) {

        }
        return map;
    }
}
