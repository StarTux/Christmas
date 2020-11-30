package com.cavetale.christmas.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class Cal {
    private Cal() { }

    public static int today() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTime(new Date());
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (month != 11 || day < 1) return -1;
        if (day > 25) return 25;
        return day;
    }

    public static int hoursLeft() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTime(new Date());
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        return 24 - hours;
    }
}
