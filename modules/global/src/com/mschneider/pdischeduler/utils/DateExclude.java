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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class DateExclude {

    private static final Logger logger = LoggerFactory.getLogger(DateExclude.class);

    private static final String DATE_PATTERN = "[2][0][0-9][0-9]-[0-1][0-9]-[0-3][0-9]";

    @SuppressWarnings("RedundantCollectionOperation")
    public static boolean checkExcluded(String refDate, String exclStr) {
        if (refDate != null && exclStr != null && refDate.matches(DATE_PATTERN)) {
            // make real checks
            List<String> items = Arrays.asList(exclStr.split(","));
            for (String item : items) {
                // logger.info("item: " + item);
                if (item.contains(":")) {
                    // date range
                    List<String> range = Arrays.asList(item.split(":"));
                    if (range.size() == 2) {
                        String start = range.get(0).trim();
                        String stop = range.get(1).trim();
                        // logger.info("start: " + start);
                        // logger.info("stop: " + stop);
                        if (start.matches(DATE_PATTERN) && stop.matches(DATE_PATTERN)
                                && (refDate.compareTo(start) >= 0 && refDate.compareTo(stop) <= 0)) {
                            return true;
                        }
                    }
                } else {
                    // single date
                    String single = item.trim();
                    // logger.info("single: " + single);
                    if (single.matches(DATE_PATTERN) && single.equals(refDate)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkExcluded(Date refDate, String exclStr) {
        if (refDate != null && exclStr != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return (checkExcluded(df.format(refDate), exclStr));
        }
        return false;
    }

    @SuppressWarnings("RedundantCollectionOperation")
    public static boolean validateExclStr(String exclStr) {
        logger.debug("validateExclStr: start");
        if (exclStr != null) {
            List<String> items = Arrays.asList(exclStr.split(","));
            for (String item : items) {
                if (item.contains(":")) {
                    // date range
                    List<String> range = Arrays.asList(item.split(":"));
                    if (range.size() == 2) {
                        String start = range.get(0).trim();
                        String stop = range.get(1).trim();
                        if (!start.matches(DATE_PATTERN) || !stop.matches(DATE_PATTERN)
                                || start.compareTo(stop) > 0) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // single date
                    String single = item.trim();
                    if (!single.matches(DATE_PATTERN)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
