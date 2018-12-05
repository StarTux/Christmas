package com.cavetale.christmas;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final class Cal {
    private Cal() { }

    public static int today() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTime(new Date());
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (month != 11 || day < 1 || day > 25) return -1;
        return day;
    }
}
