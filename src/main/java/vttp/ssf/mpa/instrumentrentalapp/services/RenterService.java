package vttp.ssf.mpa.instrumentrentalapp.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.models.UserRegistration;
import vttp.ssf.mpa.instrumentrentalapp.repositories.RentRepository;

// service for renter functions

@Service
public class RenterService {

    @Autowired
    private RentRepository rentRepo;

    @Autowired
    private SharedService sharedSvc;

    @Autowired
    private UserService userSvc;

    // for tracking
    private Logger logger = Logger.getLogger(RenterService.class.getName());

    // HELPERS

    // calculate the distance between renter & owner address for one listing
    public double calcDistance(double renterLat, double renterLong, RentalListing listing) {

        // Radius of the earth in km
        final int earthRadius = 6371;

        // get owner lat/long
        double ownerLat = listing.getLatitude();
        double ownerLong = listing.getLongitude();

        // calc lat distance (absolute) and convert to radians
        double latDistance = Math.toRadians(Math.abs(ownerLat - renterLat));

        // calc long distance (absolute) and convert to radians
        double longDistance = Math.toRadians(Math.abs(ownerLong - renterLong));

        // implement haversine formula
        // sin^2(latdist/2) + cos(renterLat) * cos(ownerLat) * sin^2(longdist/2)
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
        Math.cos(Math.toRadians(renterLat)) * Math.cos(Math.toRadians(ownerLat)) *
        Math.sin(longDistance / 2) * Math.sin(longDistance / 2);

        // calc great-circle distance between renter and owner locations
        // central angle: arctangent of sqrt of a / sqrt of 1-a
        // multiply central angle by 2 to get full central angle btw both points
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // return distance in km (btw listing add and renter add)
        return earthRadius * c;

    }

    // MAIN LISTING FUNCTIONS

    // get all listings from redis (for renters to browse)
    public List<RentalListing> getAllListings() {

        // get map from redis
        Map<Object, Object> redisMap = rentRepo.getAllListings();

        // new list to store listings
        List<RentalListing> listings = new ArrayList<>();

        // for each map entry
        for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {

            // convert jsonstring (entry value) to rentallisting
            RentalListing listing = sharedSvc.convJsonToListing((String) entry.getValue());

            // add listing to list of listings
            listings.add(listing);

        }

        return listings;

    }

    // SORT/SEARCH/FILTER FUNCTIONS

    // filter listings according to search radius - defaults to 50km (SG max radius)
    public List<RentalListing> getRadiusListings(double radius, String username, List<RentalListing> listings) {

        // get user from username
        UserRegistration user = userSvc.retrieveUser(username);

        // if default, return listings
        if (radius == 50) {
            return listings;
        }

        // else return filtered listings by dist
        return listings.stream() // stream
            .filter(listing -> calcDistance(user.getLatitude(), user.getLongitude(), listing) <= radius) // distance must be less than or equal to max radius
            .collect(Collectors.toList()); // collect to list

    }

    // sort listings
    public List<RentalListing> sortListings(String sorter, String username, List<RentalListing> listings) {

        // get user from username
        UserRegistration user = userSvc.retrieveUser(username);

        switch (sorter) {

            case "latest":
                listings.sort(Comparator.comparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder()))); // default to latest > oldest listings
                break;

            case "distance":
                listings.sort(Comparator.comparingDouble((RentalListing listing) -> calcDistance(user.getLatitude(), user.getLongitude(), listing))  // nearest to furthest, set dates latest to oldest
                .thenComparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder())));
                break;

            case "available":
                listings.sort(Comparator.comparing(RentalListing::getIsAvailable)  // nearest to furthest, set dates latest to oldest
                .thenComparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder())));
                break;

            case "fees-low-to-high":
                listings.sort(Comparator.comparingDouble(RentalListing::getFees)
                    .thenComparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder()))); // fees low to high, set dates latest to oldest
                break;

            case "fees-high-to-low":
                listings.sort(Comparator.comparingDouble(RentalListing::getFees).reversed()
                    .thenComparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder()))); // fees high to low, set dates latest to oldest
                break;

            default:
                listings.sort(Comparator.comparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder()))); // default to latest > oldest listings
                break;

        }

        return listings;

    }

    // filter listings
    public List<RentalListing> filterListings(String filter, List<RentalListing> listings) {

        if (filter.equals("all")) {
            return listings; // no filter by default
        }

        return listings.stream() // stream
            .filter(listing -> listing.getInstrumentType().equals(filter)) // filter by filter value
            .collect(Collectors.toList()); // collect to list

    }

    // for user to exclude own listings from browseListings
    public List<RentalListing> excludeOwnListings(String excludeOwn, String username, List<RentalListing> listings) {

        // if value is false, return listings
        if (excludeOwn.equals("false")) {
            return listings;
        }

        // option for user to exclude own listings from browseListings
        return listings.stream() // stream
            .filter(listing -> !listing.getOwnerName().equals(username)) // filter those that don't match user's name
            .collect(Collectors.toList()); // collect to list

    }

    // for user to exclude own listings from browseListings
    public List<RentalListing> onlyAvailListings(String onlyAvail, List<RentalListing> listings) {

        // if value is false, return listings
        if (onlyAvail.equals("false")) {
            return listings;
        }

        return listings.stream() // stream
            .filter(listing -> listing.getIsAvailable() == true) // filter available listings
            .collect(Collectors.toList()); // collect to list

    }

    // for user to search and query browseListings
    public List<RentalListing> searchListings(String searchQuery, List<RentalListing> listings) {

        // if search null, return listings
        if (searchQuery == null || searchQuery.isEmpty()) {
            return listings;
        }

        // search query to allow user to search listings
        return listings.stream() // stream
            .filter(listing -> listing.getOwnerName().toLowerCase().contains(searchQuery.toLowerCase()) // filter according to searchable fields
                || listing.getTitle().toLowerCase().contains(searchQuery.toLowerCase())
                || listing.getInstrumentType().toLowerCase().contains(searchQuery.toLowerCase())
                || listing.getInstrumentBrand().toLowerCase().contains(searchQuery.toLowerCase())
                || listing.getInstrumentModel().toLowerCase().contains(searchQuery.toLowerCase()) 
                || listing.getDescription().toLowerCase().contains(searchQuery.toLowerCase())
                || listing.getNeighborhood().toLowerCase().contains(searchQuery.toLowerCase())) 
            .collect(Collectors.toList()); // collect to list

    }

    // getting cached listings
    public List<RentalListing> getCachedListings(String cacheKey) {
        
        // cached map
        Map<Object, Object> cachedMap = rentRepo.getAllCachedQueries(cacheKey);

        // new list to store cached listings
        List<RentalListing> listings = new ArrayList<>();

        // for each map entry
        for (Map.Entry<Object, Object> entry : cachedMap.entrySet()) {

            // convert jsonstring (entry value) to rentallisting
            RentalListing listing = sharedSvc.convJsonToListing((String) entry.getValue());

            // add listing to list of listings
            listings.add(listing);

        }

        return listings;
    
    }

    // get browselistings with all functions
    public List<RentalListing> getBrowseListings(String username, String search, String filter, String sorter, String onlyAvail, String excludeOwn, int radius, int page, int size) {

        // gen cachekey to query redis
        String cacheKey = "cached:%s,%s,%s,%s,%s,%d,%d,%d".formatted(search, filter, sorter, onlyAvail, excludeOwn, radius, page, size);

        // get cached listings
        List<RentalListing> cachedListings = getCachedListings(cacheKey);

        // new listing to store redis data
        List<RentalListing> allListings = new ArrayList<>();

        // if cachekey exists in redis, get data from redis cache; else, get data from redis storage
        if (cachedListings != null && !cachedListings.isEmpty()) {
            allListings = cachedListings;
            logger.info(">>> Retrieving from Cache...");
        } else {
            allListings = getAllListings();
            logger.info(">>> Retrieving from Storage...");
        }

        // tracking/debugging
        // logger.info(">>> Retrieved listings from Redis: %s".formatted(allListings));
        
        // filter listings
        List<RentalListing> filteredListings = filterListings(filter, allListings);
        
        // sort listings
        List<RentalListing> sortListings = sortListings(sorter, username, filteredListings);

        // exclude listings
        List<RentalListing> excludeListings = excludeOwnListings(excludeOwn, username, sortListings);

        // display only avail listings
        List<RentalListing> availListings = onlyAvailListings(onlyAvail, excludeListings);

        // search listings
        List<RentalListing> searchListings = searchListings(search, availListings);

        // radius listings
        List<RentalListing> browseListings = getRadiusListings(radius, username, searchListings);

        // save listings in cache if not alr in cache
        if ((cachedListings == null || cachedListings.isEmpty()) && !(search == null || search.isEmpty())) { // only cache when users use search to keep results fresh and reduce redundancy
            browseListings.forEach(listing -> { 
                rentRepo.cacheQuery(cacheKey, listing.getListingId(), sharedSvc.convListingToJson(listing.getOwnerName(), listing));
            });    
        }

        // tracking/debugging
        // logger.info(">>> Listings after browse functions: %s".formatted(browseListings));

        return browseListings;
    }

    // LIKE LISTING FUNCTIONS

    // check if listing liked by user
    public boolean isLiked(String listingId, String username) {
        if ((listingId == null || listingId.isEmpty()) || (username == null || username.isEmpty())) {
            return false;
        }

        return rentRepo.isLikedUser(username, listingId);

    }

    // evaluate like - if liked, unlike; if not liked, like listing
    public boolean evaluateLike(String username, String listingId) {
        if (!isLiked(listingId, username)) {
            rentRepo.saveUserLike(username, listingId); // save under user
            rentRepo.saveListingLike(listingId, username); // save to like count
            return true; // return true if like
        } else {
            rentRepo.delUserLike(username, listingId);
            rentRepo.delListingLike(listingId, username);
            return false; // return false if unlike
        }
    }

    // get set of liked listing ids
    public Set<String> getUserLikesIds(String username) {
        // get set string of ids
        return rentRepo.getUserLikes(username);
    }

    // retrieve user liked listings - return empty if null
    public List<RentalListing> getUserLikes(String username) {

        // get set string of ids
        Set<String> likedListingIds = getUserLikesIds(username);

        // stream ids, get each listing, collect to list
        List<RentalListing> likedListings =  likedListingIds.stream()
            .map(id -> sharedSvc.getListing(id))
            .collect(Collectors.toList());

        if (likedListings == null || likedListings.isEmpty()) {
            return new ArrayList<>();
        }

        return likedListings;

    }

    // for renter's prefilled message when clicking contact links
    public String genContactMessage(String appUrl, String listingId, String username) {

        // get listing and profile url 
        String listingUrl = "%shome/listing/%s".formatted(appUrl, listingId);
        String profileUrl = "%sprofile/%s".formatted(appUrl, username);

        // debug
        logger.info(">>> Generated message URLs: %s / %s".formatted(listingUrl, profileUrl));

        // build prefilled message for contact links
        return UriUtils.encode("Hi! I am interested in your listing: %s. View my profile here: %s.".formatted(listingUrl, profileUrl), StandardCharsets.UTF_8);
    
    }
    
}
