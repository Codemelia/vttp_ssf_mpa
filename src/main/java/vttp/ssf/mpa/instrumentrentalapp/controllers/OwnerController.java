package vttp.ssf.mpa.instrumentrentalapp.controllers;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.services.OwnerService;
import vttp.ssf.mpa.instrumentrentalapp.services.SharedService;

// for owner-centric functions

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private SharedService sharedSvc;

    @Autowired
    private OwnerService ownerSvc;
    
    // get owner listings page
    @GetMapping
    public ModelAndView getListingPage(HttpSession session) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // new array list to store listings
        List<RentalListing> ownerListings = ownerSvc.getAllOwnerListings(username);

        // get likes/waitlists for ownerListings
        Map<String, Long> likesMap = sharedSvc.getListingLikesMap(ownerListings);

        // get owner listing count
        long listingCount = ownerSvc.getListingCount(username);
        
        return new ModelAndView("owner")
            .addObject("ownerListings", ownerListings)
            .addObject("likesMap", likesMap)
            .addObject("username", username)
            .addObject("listingCount", listingCount);
    }

    // create owner listing
    @GetMapping("/create")
    public ModelAndView createListing() {

        // return listing form with new rentallisting object
        return new ModelAndView("createlisting")
            .addObject("listing", new RentalListing());

    }

    // post owner listing
    @PostMapping("/submit")
    public ModelAndView submitListing(@Valid @ModelAttribute("listing") RentalListing listing, BindingResult binding, HttpSession session, RedirectAttributes redirectAttributes) {

        // if there are validation errors
        if (binding.hasErrors()) {
            // return to page with errors
            return new ModelAndView("createlisting");
        }

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        try {

            // when owner submit listing, save to redis
            ownerSvc.saveListing(username, listing);

            // add success message
            redirectAttributes.addFlashAttribute("successMessage", "Listing saved successfully!");

        } catch (Exception e) {
            // any exceptions occur during saving, add fail message
            redirectAttributes.addFlashAttribute("failMessage", "Listing failed to save:%s".formatted(e.getMessage()));
            
        }

        // after submission, go to owner dashboard
        return new ModelAndView("redirect:/owner");

    }

    // retrieve listing to update
    @GetMapping("/edit/{listingId}")
    public ModelAndView editListing(@PathVariable String listingId, RedirectAttributes redirectAttributes) {

        try{
            // get listing
            RentalListing listing = sharedSvc.getListing(listingId);

            System.out.println(listing.getContactMethods());

            // add listing to form view
            return new ModelAndView("createlisting")
                .addObject("listing", listing);

        } catch (Exception e) {
            // any exceptions occur during saving, add fail message
            redirectAttributes.addFlashAttribute("failMessage", "Failed to retrieve listing: %s.".formatted(e.getMessage()));

            // return to owner page
            return new ModelAndView("redirect:/owner");
            
        }

    }

    // delete listing using rest endpoint - don't need message feedback
    @PostMapping(path="/delete/{listingId}", produces="text/plain")
    @ResponseBody
    public ResponseEntity<String> deleteListing(@PathVariable String listingId, HttpSession session) {

        // get username from sess
        String username = (String) session.getAttribute("authenticatedUser");

        // delete listing
        ownerSvc.deleteListing(username, listingId);

        // javascript handling on html to do a pop-up delete confirm message before user deletes
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/owner")); // stay on owner page
        headers.add("X-Success-Message", "Listing successfully deleted"); // add a message

        // return responseentity with status 302 for redirect
        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }

}
