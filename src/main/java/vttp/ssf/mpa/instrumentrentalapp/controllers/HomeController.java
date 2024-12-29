package vttp.ssf.mpa.instrumentrentalapp.controllers;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import vttp.ssf.mpa.instrumentrentalapp.components.TimeFormatter;
import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.models.UserProfile;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.PaginatedResult;
import vttp.ssf.mpa.instrumentrentalapp.services.MapsApiService;
import vttp.ssf.mpa.instrumentrentalapp.services.RenterService;
import vttp.ssf.mpa.instrumentrentalapp.services.SharedService;
import vttp.ssf.mpa.instrumentrentalapp.services.UserService;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private SharedService sharedSvc;

    @Autowired
    private MapsApiService mapsSvc;

    @Autowired
    private RenterService renterSvc;

    @Autowired
    private UserService userSvc;

    // for contact links
    @Value("${app.domain.url}")
    private String appUrl;

    // for dateUpdated formatting time across methods
    private TimeFormatter timeFormatter = new TimeFormatter();
    
    // access Homepage
    @GetMapping
    public ModelAndView getHomepage(@RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "onlyAvail", defaultValue = "false") String onlyAvail,
        @RequestParam(value = "excludeOwn", defaultValue = "false") String excludeOwn, 
        @RequestParam(value = "filter", defaultValue = "all") String filter, 
        @RequestParam(value = "sorter", defaultValue = "latest") String sorter,
        @RequestParam(value = "radius", defaultValue = "50") int radius,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size, 
        HttpSession session, RedirectAttributes redirectAttributes) {

        // get user username from session
        String username = (String) session.getAttribute("authenticatedUser");

        try {

            // get listings from cached query else redis storage and implement search/sort/filter functions
            List<RentalListing> browseListings = renterSvc.getBrowseListings(username, search, filter, sorter, onlyAvail, excludeOwn, radius, page, size);

            // paginate listings after search/sort/filter
            PaginatedResult<RentalListing> paginatedListings = sharedSvc.paginateListings(browseListings, page, size);
            browseListings = paginatedListings.getItems();

            // get liked listing ids
            Set<String> userLikesIds = renterSvc.getUserLikesIds(username);
            
            // get user's liked listings
            List<RentalListing> userLikes = renterSvc.getUserLikes(username);

            // get likesMap for userLikes
            Map<String, Long> likesMap = sharedSvc.getListingLikesMap(userLikes);

            // get likesMap for browselistings
            Map<String, Long> allLikesMap = sharedSvc.getListingLikesMap(browseListings);

            // pass username, browseListings, nearbyListings to homepage
            return new ModelAndView("homepage")
                .addObject("username", username)
                .addObject("browseListings", browseListings)
                .addObject("timeFormatter", timeFormatter)
                .addObject("filter", filter)
                .addObject("sorter", sorter)
                .addObject("excludeOwn", excludeOwn)
                .addObject("onlyAvail", onlyAvail)
                .addObject("search", search)
                .addObject("radius", radius)
                .addObject("likesMap", likesMap)
                .addObject("allLikesMap", allLikesMap)
                .addObject("userLikes", userLikes)
                .addObject("userLikesIds", userLikesIds)
                .addObject("currentPage", page)
                .addObject("totalPages", paginatedListings.getTotalPages());

        } catch (Exception e) {

            // add error message as redir attr
            redirectAttributes.addFlashAttribute("errMessage", e.getMessage());

            // return to home page
            return new ModelAndView("redirect:/home");

        }

    }

    // to view individual listing
    @GetMapping("/listing/{listingId}")
    public ModelAndView getListing(@PathVariable String listingId, HttpSession session, RedirectAttributes redirectAttributes) {

        if (listingId == null || listingId.trim().isEmpty()) {
            // Handle invalid listingId
            redirectAttributes.addFlashAttribute("errMessage", "Error: Invalid listing ID.");
            return new ModelAndView("redirect:/home");
        } 

        try {
            // get listing from redis
            RentalListing listing = sharedSvc.getListing(listingId);

            // get static map url from static map api
            String staticMapUrl = mapsSvc.getStaticMap(listing);

            // get username from session
            String username = (String) session.getAttribute("authenticatedUser");

            System.out.println("Username: " + username);

            // check if listing liked by user
            boolean isLiked = renterSvc.isLiked(listingId, username);

            // get userprofile of listing owner
            UserProfile ownerProfile = userSvc.getProfile(listing.getOwnerName());

            // get listing likes
            long listingLikes = sharedSvc.getListingLikes(listing);

            // gen prefilled message for contact links
            String contactMsg = renterSvc.genContactMessage(appUrl, listingId, username);

            // go to view listing with listing and owner number and static map url of owner location
            return new ModelAndView("viewlisting")
                .addObject("listing", listing)
                .addObject("staticMapUrl", staticMapUrl)
                .addObject("timeFormatter", timeFormatter)
                .addObject("listingLikes", listingLikes)
                .addObject("isLiked", isLiked)
                .addObject("ownerProfile", ownerProfile)
                .addObject("contactMsg", contactMsg)
                .addObject("username", username);

        } catch (Exception e) {

            // add error message as redir attr
            redirectAttributes.addFlashAttribute("errMessage", e.getMessage());

            // return to home page
            return new ModelAndView("redirect:/home");

        }

    }

    // rest endpoint for liking/unliking listings - no need return message as feedback can be seen via like/unlike btns
    @PostMapping(path="/listing/{listingId}/like", produces="text/plain")
    @ResponseBody
    public ResponseEntity<String> likeListing(@PathVariable String listingId, HttpSession session, RedirectAttributes redirectAttributes) {

        // get username from session
        String username = (String) session.getAttribute("authenticatedUser");

        // if not liked, like it; else, unlike it
        boolean isLiked = renterSvc.evaluateLike(username, listingId);
        
        // new headers to set location
        HttpHeaders headers = new HttpHeaders();
        
        headers.setLocation(URI.create("/home")); // go to home page for visual feedback

        // add header according to boolean
        if (isLiked) {
            headers.add("X-Success-Message", "Listing %s liked".formatted(listingId));
        } else {
            headers.add("X-Success-Message", "Listing %s unliked".formatted(listingId));
        }

        // return responseentity with status 302 for redirect
        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }
    
}
