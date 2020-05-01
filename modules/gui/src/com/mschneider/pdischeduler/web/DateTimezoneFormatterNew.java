package com.mschneider.pdischeduler.web;

import com.mschneider.pdischeduler.utils.TimeZoneUtils;

import java.util.Date;
import java.util.function.Function;

public class DateTimezoneFormatterNew implements Function<Object, String> {

    private final String timezone;

    public DateTimezoneFormatterNew(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String apply(Object o) {
        if (o != null && timezone != null) {
            return TimeZoneUtils.strFromUtcDateConvertToTimezone((Date) o, timezone);
        }
        return null;
    }
}
