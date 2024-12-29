package vttp.ssf.mpa.instrumentrentalapp.services;

import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.ssf.mpa.instrumentrentalapp.models.CalendarBooking;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.CalendarDay;
import vttp.ssf.mpa.instrumentrentalapp.repositories.CalendarRepository;

// service methods for calendar

@Service
public class CalendarService {

    @Autowired
    private CalendarRepository calendarRepo;

    // HELPERS

    // convert booking to jsonstring
    public String convBookingToJson(String username, CalendarBooking booking) {

        // build jsonobject
        JsonObject jObject = Json.createObjectBuilder()
            .add("bookingId", booking.getBookingId())
            .add("title", booking.getTitle())
            .add("startDate", booking.getStartDate())
            .add("endDate", booking.getEndDate())
            .build();

        // return jsonstring
        return jObject.toString();

    }
        
    // conv jsonstring to booking
    public CalendarBooking convJsonToBooking(String username, String jsonString) {

        // read json string to json object
        JsonReader jReader = Json.createReader(new StringReader(jsonString));
        JsonObject jObject = jReader.readObject();

        // map values to booking and return
        return new CalendarBooking(jObject.getString("bookingId"), 
            jObject.getString("title"), 
            jObject.getString("startDate"), 
            jObject.getString("endDate"));
        
    }

    // MAIN BOOKING FUNCTIONS

    // save booking to redis
    public void saveBooking(String username, CalendarBooking booking) {

        // get bookingId
        String bookingId = booking.getBookingId();
        
        // if bookingId null, gen and set new UUID
        if (bookingId == null || bookingId.isEmpty()) {
            bookingId = UUID.randomUUID().toString().substring(0, 8);
            booking.setBookingId(bookingId);
        }

        // conv booking to jsonstring
        String jString = convBookingToJson(username, booking);

        // save booking
        calendarRepo.saveBooking(username, bookingId, jString);

    }
    
    // retrieve booking from redis
    public CalendarBooking getBooking(String username, String bookingId) {

        // get booking as string
        String jString = calendarRepo.getBooking(username, bookingId);

        // conv jsonstring to booking
        return convJsonToBooking(username, jString);

    }

    // delete booking from redis
    public void delBooking(String username, String bookingId) {
        calendarRepo.delBooking(username, bookingId);
    }

    // get all bookings
    public List<CalendarBooking> getAllBookings(String username) {

        // get bookings as map<id, bookingstring>
        Map<Object, Object> bookMap = calendarRepo.getAllBookings(username);

        // new list to hold values
        List<CalendarBooking> bookings = new ArrayList<>();

        // return map as list
        for (Map.Entry<Object, Object> entry : bookMap.entrySet()) {
            String jString = (String) entry.getValue();
            CalendarBooking booking = convJsonToBooking(username, jString);
            bookings.add(booking);
        }

        return bookings;

    }

    // to generate calendar
    public List<CalendarDay> genCalendar(int year, int month) {

        // new list of cal days
        List<CalendarDay> days = new ArrayList<>();

        // get first day of current/specified year/month
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        // get start day of week
        DayOfWeek startDayOfWeek = firstDayOfMonth.getDayOfWeek();

        // get off set from start day - remaining days to be left blank before first day
        int offset = startDayOfWeek.getValue() % 7;

        // add blank days before first day
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay(null, false)); // set to null and false
        }

        // add all days in month
        for (int day = 1; day <= firstDayOfMonth.lengthOfMonth(); day++) {
            days.add(new CalendarDay(LocalDate.of(year, month, day), true)); // set date and true (current month)
        }

        // add blank days after last day
        int remainingDays = 7 - (days.size() % 7); // 7 - remainder of total size / 7
        for (int i = 0; i < remainingDays; i++) {
            days.add(new CalendarDay(null, false)); // set to null and false
        }

        return days;
    }

    // split days into weeks to display calendar
    public List<List<CalendarDay>> splitIntoWeeks(List<CalendarDay> days) {

        // new lists to hold data
        List<List<CalendarDay>> weeks = new ArrayList<>();
        List<CalendarDay> currentWeek = new ArrayList<>();
        
        // add each day into current week
        for (CalendarDay day : days) {

            currentWeek.add(day);
            
            // if current week size = 7 (1 full week)
            if (currentWeek.size() == 7) {

                // new arraylist, add current week into weeks
                weeks.add(new ArrayList<>(currentWeek));

                // clear current week to reiterate
                currentWeek.clear();
            }
        }
        
        // add the last week if it contains less than 7 days
        if (!currentWeek.isEmpty()) {
            weeks.add(currentWeek);
        }
        
        return weeks;
    }    

    // parse iso string to localdates
    public LocalDate parseIsoToLocalDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
    }

    // get days between booking start/end dates
    public Map<String, List<LocalDate>> getBookingDays(List<CalendarBooking> bookings) {

        // new map to store booking days
        Map<String, List<LocalDate>> bookingDays = new HashMap<>();

        for (CalendarBooking booking : bookings) {
            
            // parse start and end to localdate for thymeleaf processing
            LocalDate start = parseIsoToLocalDate(booking.getStartDate());
            LocalDate end = parseIsoToLocalDate(booking.getEndDate());

            // get each booking id
            String bookingId = booking.getBookingId();

            // new list to store dates
            List<LocalDate> dates = new ArrayList<>();

            // look through start > end excluding start and end, add each date to list of dates
            for (LocalDate date = start.plusDays(1); date.isBefore(end); date = date.plusDays(1)) {
                dates.add(date);
            }

            // for each booking, map id to list of dates
            bookingDays.put(bookingId, dates);

        }

        return bookingDays;

    }

}
