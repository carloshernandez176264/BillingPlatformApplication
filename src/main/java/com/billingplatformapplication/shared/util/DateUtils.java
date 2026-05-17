package com.billingplatformapplication.shared.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.YearMonth;

@UtilityClass
public class DateUtils {

    public static int workingDaysInMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int count = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            switch (date.getDayOfWeek()) {
                case SATURDAY, SUNDAY -> { /* skip */ }
                default -> count++;
            }
        }
        return count;
    }

    public static LocalDate firstDayOf(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate lastDayOf(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth();
    }
}

