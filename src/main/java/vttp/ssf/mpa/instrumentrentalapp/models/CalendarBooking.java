package vttp.ssf.mpa.instrumentrentalapp.models;

// for calendar
public class CalendarBooking {
    
    private String bookingId;
    
    // required fields handled by html, other validation handled in controller as well to keep code neat
    private String title;
    private String startDate; // will be in ISO format
    private String endDate;
    
    public String getBookingId() {
        return bookingId;
    }
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public CalendarBooking() {
    }

    public CalendarBooking(String bookingId, String title, String startDate, String endDate) {
        this.bookingId = bookingId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "CalendarBooking [bookingId=" + bookingId + ", title=" + title + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }

}
