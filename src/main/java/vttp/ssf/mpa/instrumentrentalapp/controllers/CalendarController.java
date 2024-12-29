package vttp.ssf.mpa.instrumentrentalapp.controllers;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import vttp.ssf.mpa.instrumentrentalapp.models.CalendarBooking;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.CalendarDay;
import vttp.ssf.mpa.instrumentrentalapp.services.CalendarService;

// controller for calendar

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarSvc;
    
    // display calendar
    @GetMapping
    public ModelAndView viewCalendar(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, HttpSession session) {

        // get year from param, if not set year and month by sys date
        LocalDate today = LocalDate.now();
        year = (year != null) ? year : today.getYear();
        month = (month != null) ? month : today.getMonthValue();

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // get monthName
        String monthName = Month.of(month).name().substring(0, 3); // e.g., JAN, FEB

        // get list of weeks and bookings
        List<CalendarDay> days = calendarSvc.genCalendar(year, month);
        List<List<CalendarDay>> weeks = calendarSvc.splitIntoWeeks(days);
        List<CalendarBooking> bookings = calendarSvc.getAllBookings(username);

        // create a map of booking id > dates between each booking start/end date
        Map<String, List<LocalDate>> bookingDays = calendarSvc.getBookingDays(bookings);

        return new ModelAndView("calendar")
            .addObject("bookings", bookings)
            .addObject("weeks", weeks)
            .addObject("year", year)
            .addObject("month", month)
            .addObject("bookingDays", bookingDays)
            .addObject("monthName", monthName)
            .addObject("username", username)
            .addObject("booking", new CalendarBooking());
    }

    // add new booking
    @PostMapping("/save")
    public ModelAndView saveBooking(@ModelAttribute("booking") CalendarBooking booking, RedirectAttributes redirectAttributes, HttpSession session) throws Exception {

        // get dates
        LocalDate startDate = calendarSvc.parseIsoToLocalDate(booking.getStartDate());
        LocalDate endDate = calendarSvc.parseIsoToLocalDate(booking.getEndDate());

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        try {

            // validate dates - disallow startdate from being after enddate
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date."); 
            }

            // validate title - disallow > 50 chars for purpose of design
            if (booking.getTitle().length() > 50) {
                throw new IllegalArgumentException("Title length must not exceed 50 characters.");
            }

            // save booking from form
            calendarSvc.saveBooking(username, booking);

            // add success message and blank form to view
            redirectAttributes.addFlashAttribute("successMessage", "Booking saved successfully!");
        
        } catch (Exception e) {

            // add fail message and current form to view
            redirectAttributes.addFlashAttribute("failMessage", "Error saving booking: %s".formatted(e.getMessage())); 

        }

        return new ModelAndView("redirect:/calendar");  // back to the calendar view
    }

    // delete booking - rest endpoint (don't need message feedback)
    @PostMapping("/delete/{bookingId}")
    @ResponseBody
    public ResponseEntity<String> delBooking(@PathVariable String bookingId, HttpSession session) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // delete booking from redis
        calendarSvc.delBooking(username, bookingId);

        // javascript handling on html to do a pop-up delete confirm message before user deletes
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/calendar")); // stay on calendar page
        headers.add("X-Success-Message", "Booking successfully deleted"); // add a message

        // return responseentity with status 302 for redirect
        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }

    // edit booking
    @GetMapping("/edit/{bookingId}")
    public ModelAndView getBookingEdit(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, @PathVariable String bookingId, RedirectAttributes redirectAttributes, HttpSession session) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // get year from param, if not set year and month by sys date
        LocalDate today = LocalDate.now();
        year = (year != null) ? year : today.getYear();
        month = (month != null) ? month : today.getMonthValue();

        // get monthName
        String monthName = Month.of(month).name().substring(0, 3); // e.g., JAN, FEB

        // get list of weeks and bookings
        List<CalendarDay> days = calendarSvc.genCalendar(year, month);
        List<List<CalendarDay>> weeks = calendarSvc.splitIntoWeeks(days);
        List<CalendarBooking> bookings = calendarSvc.getAllBookings(username);

        // create a map of booking id > dates between each booking start/end date
        Map<String, List<LocalDate>> bookingDays = calendarSvc.getBookingDays(bookings);

        // new mav
        ModelAndView mav = new ModelAndView("calendar");

        // show a message for retrieval
        try {

            // get booking
            CalendarBooking booking = calendarSvc.getBooking(username, bookingId);

            // add booking to view
            mav.addObject("booking", booking)
                .addObject("successMessage", "Edit your booking here!");

        } catch (Exception e) {
            // any exceptions occur during retrieval, add fail message
            mav.addObject("failMessage", "Failed to retrieve booking: %s.".formatted(e.getMessage()));
            mav.setStatus(HttpStatus.BAD_REQUEST); // set to 400
        }

        // return form view
        return mav.addObject("bookings", bookings)
            .addObject("weeks", weeks)
            .addObject("year", year)
            .addObject("month", month)
            .addObject("bookingDays", bookingDays)
            .addObject("monthName", monthName);

    }

}
