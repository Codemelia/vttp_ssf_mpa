package vttp.ssf.mpa.instrumentrentalapp.services;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.repositories.RentRepository;

// service for owner functions

@Service
public class OwnerService {

    @Autowired
    private RentRepository rentRepo;

    @Autowired
    private SharedService sharedSvc;

    // save listing to redis when owner creates one
    public void saveListing(String ownerName, RentalListing listing) {

        String listingId = listing.getListingId();

        // if listing is null/empty, gen new uuid
        if (listingId == null || listingId.isEmpty()) {
            listingId = UUID.randomUUID().toString().substring(0, 12); // 12 chars to ensure uniqueness
            listing.setListingId(listingId); // set new listing id to listing if not done yet
        } 

        LocalDate dateCreated = listing.getDateCreated();

        // if date not set, set new date
        if (dateCreated == null) {
            dateCreated = LocalDate.now();
            listing.setDateCreated(dateCreated); // set curr date if not set yet
        }

        // set new date each time listing is saved
        ZonedDateTime dateUpdated = ZonedDateTime.now();
        listing.setDateUpdated(dateUpdated); // set updated date

        // conv listing to jsonstring
        String listingJString = sharedSvc.convListingToJson(ownerName, listing);

        // save to redis under listingId for storage/retrieval
        rentRepo.saveListing(listing.getListingId(), listingJString);

        // save listingId under user in redis
        rentRepo.saveListingId(ownerName, listingId);

    }
    

    // get list of listings under owner from redis
    public List<RentalListing> getAllOwnerListings(String ownerName) {

        // get map of <listingId, listingString> from redis
        Map<Object, Object> redisMap = rentRepo.getAllListings();

        // new list to store owner's listings
        List<RentalListing> listings = new ArrayList<>();

        // for each entry from map
        for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {

            // convert jsonstring (entry value) to listing
            RentalListing listing = sharedSvc.convJsonToListing((String) entry.getValue());

            // if listing id starts with given username, add it to list
            if (listing.getOwnerName().equals(ownerName)) {
                listings.add(listing);
            }

        }

        // sort listings according to dateUpdated
        listings.sort(Comparator.comparing(RentalListing::getDateUpdated, Comparator.nullsLast(Comparator.reverseOrder())));

        // return list of owner's listings
        return listings;

    }

    // delete listing from redis
    public void deleteListing(String ownerName, String listingId) {

        // delete from redis
        rentRepo.deleteListing(listingId);

        // delete listingId from user in redis
        rentRepo.delListingId(ownerName, listingId);

    }

    // save listingid under user in redis
    public void saveListingId(String username, String listingId) {
        rentRepo.saveListingId(username, listingId);
    }

    // get listingidcount under user from redis
    public long getListingCount(String username) {
        return rentRepo.getListingIdCount(username);
    }

}
