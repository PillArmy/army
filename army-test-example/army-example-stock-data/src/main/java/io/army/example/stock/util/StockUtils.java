package io.army.example.stock.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public abstract class StockUtils {


    private StockUtils() {
    }


    public static LocalDate tradingDay() {
        final LocalDateTime now = LocalDateTime.now();
        LocalDate day = now.toLocalDate();
        if (now.toLocalTime().isBefore(LocalTime.of(15, 5, 0))) {
            day = day.minusDays(1);
        }
        return workDay(day);
    }


    private static LocalDate workDay(LocalDate day) {
        switch (day.getDayOfWeek()) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                break;
            case SATURDAY:
                day = day.minusDays(1);
                break;
            case SUNDAY:
                day = day.minusDays(2);
                break;
            default:
                throw new RuntimeException("");
        }
        return day;
    }


}
