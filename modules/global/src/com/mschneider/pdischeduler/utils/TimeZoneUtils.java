/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TimeZoneUtils {

    public static Map<String, String> getLookupList() {
        Map<String, String> map = new LinkedHashMap<>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            map.put(displayTimeZone(TimeZone.getTimeZone(id)), id);
        }
        return map;
    }

    private static String displayTimeZone(TimeZone tz) {
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours);

        String utcOffset = (hours >= 0 ? "+" : "-") + String.format("%02d:%02d", Math.abs(hours), Math.abs(minutes));
        return String.format("%s [UTC%s]", tz.getID(), utcOffset);
    }

    public static Date dateNowUtc() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        return dateFromZonedDateTimeWithTimezone(zonedDateTime, "UTC");
    }

    public static Date dateNow(String timezone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        return dateFromZonedDateTimeWithTimezone(zonedDateTime, timezone);
    }

    public static String dateNowStr(String timezone) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime zonedDateTimeTZ = zonedDateTime.withZoneSameInstant(ZoneId.of(timezone));
        return dtf.format(zonedDateTimeTZ);
    }

    public static String dateNowFileNameStr() {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        return dtf.format(zonedDateTime);
    }

    /*
     * Get Date from ZonedDateTime with specified Timezone
     *
     * Attention: If JVM is NOT running with UTC Timezone then there is a principle problem when local
     * timezone is switching from standard time to daylight saving time. This hour does not exist within
     * this timezone and therefore no Date object can be created for this hour.
     * For all other date/time it works fine also with non UTC default Timezone.
     */
    public static Date dateFromZonedDateTimeWithTimezone(ZonedDateTime zonedDateTime, String timezone) {
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
        // logger.info("dateFromZonedDateTimeWithTimezone: zonedDateTime " + dtf.format(zonedDateTime) + " OFF " + (zonedDateTime.getOffset().getTotalSeconds() / 3600));

        ZonedDateTime zonedDateTimeDstTZ = zonedDateTime.withZoneSameInstant(ZoneId.of(timezone));
        // logger.info("dateFromZonedDateTimeWithTimezone: zonedDateTimeDstTZ " + dtf.format(zonedDateTimeDstTZ) + " OFF " + (zonedDateTimeDstTZ.getOffset().getTotalSeconds() / 3600));

        ZonedDateTime zonedDateTimeDefTZ = zonedDateTimeDstTZ.withZoneSameLocal(ZoneId.systemDefault());
        // logger.info("dateFromZonedDateTimeWithTimezone: zonedDateTimeDefTZ " + dtf.format(zonedDateTimeDefTZ) + " OFF " + (zonedDateTimeDefTZ.getOffset().getTotalSeconds() / 3600));

        return new Date((zonedDateTimeDefTZ.toEpochSecond() * 1000) + (zonedDateTimeDefTZ.getNano() / 1000000));
    }

    /*
     * Get ZonedDateTime from Date with specified Timezone
     *
     * Attention: If JVM is NOT running with UTC Timezone then there is a principle problem when local
     * timezone is switching from daylight saving to standard time. This hour exists twice ...
     * For all other date/time it works fine also with non UTC default Timezone.
     */
    public static ZonedDateTime zonedDateTimeFromDateWithTimezone(Date date, String timezone) {
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");

        ZonedDateTime zonedDateTimeDefTZ = date.toInstant().atZone(ZoneId.systemDefault());
        // logger.info("zonedDateTimeFromDateWithTimezone: zonedDateTimeDefTZ " + dtf.format(zonedDateTimeDefTZ) + " OFF " + (zonedDateTimeDefTZ.getOffset().getTotalSeconds() / 3600));

        return zonedDateTimeDefTZ.withZoneSameLocal(ZoneId.of(timezone));
    }

    public static ZonedDateTime zonedDateTimeFromUtcDateConvertToTimezone(Date date, String timezone) {
        ZonedDateTime zonedDateTime = zonedDateTimeFromDateWithTimezone(date, "UTC");
        return zonedDateTime.withZoneSameInstant(ZoneId.of(timezone));
    }

    public static String strFromUtcDateConvertToTimezone(Date date, String timezone) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
        ZonedDateTime zonedDateTime = zonedDateTimeFromUtcDateConvertToTimezone(date, timezone);
        return dtf.format(zonedDateTime);
    }

}