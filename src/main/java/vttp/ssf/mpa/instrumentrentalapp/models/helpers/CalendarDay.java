package vttp.ssf.mpa.instrumentrentalapp.models.helpers;

import java.time.LocalDate;

public class CalendarDay {
    
    private LocalDate date;
    private boolean isCurrentMonth;

    public CalendarDay() {
        
    }

    public CalendarDay(LocalDate date, boolean isCurrentMonth) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }

    @Override
    public String toString() {
        return "CalendarDay [date=" + date + ", isCurrentMonth=" + isCurrentMonth + "]";
    }

}
