package vttp.ssf.mpa.instrumentrentalapp.services;

import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.models.UserRegistration;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.PaginatedResult;
import vttp.ssf.mpa.instrumentrentalapp.repositories.RentRepository;

// service for helper/shared methods for renter/owner services

@Service
public class SharedService {

    @Autowired
    private RentRepository rentRepo;

    @Autowired
    private UserService userSvc;

    // convert listing String to listing object
    public RentalListing convJsonToListing(String listingString) {

        // read jsonstring
        JsonReader jReader = Json.createReader(new StringReader(listingString));

        // convert to jsonobject
        JsonObject jObject = jReader.readObject();

        // retrieve instrument pics
        List<String> instrumentPics = new ArrayList<>();
        if (jObject.containsKey("instrumentPics") && jObject.getJsonArray("instrumentPics") != null) {
            instrumentPics = jObject.getJsonArray("instrumentPics").stream()
                .map(jsonValue -> ((JsonString) jsonValue).getString())
                .collect(Collectors.toList());
        }

        // retrieve contact methods
        List<String> contactMethods = new ArrayList<>();
        if (jObject.containsKey("contactMethods") && jObject.getJsonArray("contactMethods") != null) {
            contactMethods = jObject.getJsonArray("contactMethods").stream()
                .map(jsonValue -> ((JsonString) jsonValue).getString())
                .collect(Collectors.toList());
        }

        // map to RentalListing
        RentalListing listing = new RentalListing(
            Instant.ofEpochSecond(jObject.getJsonNumber("dateCreated").longValue()).atZone(ZoneId.systemDefault()).toLocalDate(), // retrieve as localdate
            Instant.ofEpochSecond(jObject.getJsonNumber("dateUpdated").longValue()).atZone(ZoneId.systemDefault()), // retrieve as zoneddatetime
            jObject.containsKey("listingId") ? jObject.getString("listingId") : "Unknown ID",
            jObject.containsKey("ownerName") ? jObject.getString("ownerName") : "Uknown Owner",
            jObject.containsKey("ownerNumber") ? jObject.getString("ownerNumber") : "Uknown Number",
            jObject.containsKey("title") ? jObject.getString("title") : "Untitled", 
            jObject.containsKey("instrumentType") ? jObject.getString("instrumentType") : "Others",
            jObject.containsKey("instrumentBrand") ? jObject.getString("instrumentBrand") : "Others",
            jObject.containsKey("instrumentModel") ? jObject.getString("instrumentModel") : "Others",
            instrumentPics,
            jObject.containsKey("description") ? jObject.getString("description") : "No description", 
            jObject.containsKey("fees") ? jObject.getJsonNumber("fees").doubleValue() : 0.0,
            jObject.containsKey("priceModel") ? jObject.getString("priceModel") : "hourly",
            jObject.containsKey("isAvailable") ? jObject.getBoolean("isAvailable") : true,
            contactMethods,
            jObject.containsKey("latitude") ? jObject.getJsonNumber("latitude").doubleValue() : 0.0,
            jObject.containsKey("longitude") ? jObject.getJsonNumber("longitude").doubleValue() : 0.0,
            jObject.getString("neighborhood", "No neighborhood found"));

        return listing;

    }

    // convert listing to jsonstring
    public String convListingToJson(String ownerName, RentalListing listing) {

        // get registered user from userrepo
        UserRegistration owner = userSvc.retrieveUser(ownerName);

        // get instrument brand - set default value to Others if not filled
        String instrumentBrand = (listing.getInstrumentBrand() != null && !listing.getInstrumentBrand().trim().isEmpty())
            ? listing.getInstrumentBrand() : "Others";

        // get instrument model - set default value to Others if not filled
        String instrumentModel = (listing.getInstrumentModel() != null && !listing.getInstrumentModel().trim().isEmpty())
            ? listing.getInstrumentModel() : "Others";

        // create jsonarray for contact methods
        JsonArrayBuilder contactJBuilder = Json.createArrayBuilder();
        List<String> contactMethods = listing.getContactMethods();
        if (contactMethods != null) {
            for (String c : contactMethods) { 
                if (c != null && !c.trim().isEmpty()) {
                    contactJBuilder.add(c); // skip empty/null strings
                }
            }
        }
        JsonArray contactJArray = contactJBuilder.build();

        // create jsonarray for instrument pics
        JsonArrayBuilder instruJBuilder = Json.createArrayBuilder();
        List<String> instrumentPics = listing.getInstrumentPics();
        // add non-empty URLs to the JsonArray
        if (instrumentPics != null) {
            for (String i : instrumentPics) {
                if (i != null && !i.trim().isEmpty()) {  // skip empty strings or null URLs
                    instruJBuilder.add(i);
                }
            }
        }
        JsonArray instruJArray = instruJBuilder.build();

        // map to new jObject
        JsonObject listingJObject = Json.createObjectBuilder()
            .add("dateCreated", listing.getDateCreated()
            .atStartOfDay(ZoneId.systemDefault()).toEpochSecond()) // store as epochsec
            .add("dateUpdated", listing.getDateUpdated().toEpochSecond())
            .add("listingId", listing.getListingId()) // for renter reference
            .add("ownerName", ownerName) // for renter reference
            .add("ownerNumber", owner.getNumber()) // for contact links
            .add("title", listing.getTitle()) // for renter reference
            .add("instrumentType", listing.getInstrumentType()) // for renter reference
            .add("instrumentBrand", instrumentBrand) // for renter reference
            .add("instrumentModel", instrumentModel) // for renter reference
            .add("instrumentPics", instruJArray) // for renter reference
            .add("description", listing.getDescription()) // for renter reference
            .add("fees", listing.getFees()) // for renter reference
            .add("priceModel", listing.getPriceModel()) // for renter reference
            .add("isAvailable", listing.getIsAvailable()) // for renter reference
            .add("contactMethods", contactJArray) // for contact links
            .add("latitude", owner.getLatitude()) // for static map/sorting by distance
            .add("longitude", owner.getLongitude()) // for static map/sorting by distance
            .add("neighborhood", owner.getNeighborhood()) // for map location/search funct
            .build();

        return listingJObject.toString();

    }

    // get listing from redis
    public RentalListing getListing(String listingId) {

        // find listingString by listingId
        String listingString = rentRepo.getListing(listingId);

        // conv listingString to listing object
        RentalListing listing = new RentalListing();
        if (listingString != null && !listingString.isEmpty()) {
            listing = convJsonToListing(listingString);
        }

        return listing;

    }

    // to paginate listings on homepage
    public PaginatedResult<RentalListing> paginateListings(List<RentalListing> listings, int page, int size) {

        // calc start index of sublist
        int startIndex = page * size; 

        // calc end index of sublist
        int endIndex = Math.min(startIndex + size, listings.size());

        // if start index of sublist is more than listings size
        if (startIndex > listings.size()) {
            
            // return empty page to avoid any errors
            return new PaginatedResult<>(Collections.emptyList(), 0);

        }

        // make paginated list sublist of listings
        List<RentalListing> paginatedList = listings.subList(startIndex, endIndex);

        // calc total pages needed to display all listings
        // Math.ceil rounds up the result of listings size / specified page size to an int
        int totalPages = (int) Math.ceil((double) listings.size() / size);

        // return paginated result
        return new PaginatedResult<>(paginatedList, totalPages);
    }

    // LIKE LISTING FUNCTIONS

    // retrieve listings likes map for specified listings
    public Map<String, Long> getListingLikesMap(List<RentalListing> listings) {

        Map<String, Long> likesMap = listings.stream() // stream
            .collect(Collectors.toMap( // collect to map
                listing -> listing.getListingId(),
                listing -> getListingLikes(listing))); // for each listing, map to Map<listingId, listinglikes for id>

        if (likesMap != null) {
            return likesMap;
        } else {
            return new HashMap<>();
        }

    }

    // retrieve liked listing count for indiv listing
    public long getListingLikes(RentalListing listing) {
        return rentRepo.getListingLikes(listing.getListingId());
    }

    // method for healthcheck
	public String randomKey() throws Exception {

		String randomKey = rentRepo.randomKey();

		// if no random key
		if (randomKey == null) {
			// throw exception
			throw new Exception("No randomkey in Redis keyspace");

		}

		return randomKey;

	}

}
