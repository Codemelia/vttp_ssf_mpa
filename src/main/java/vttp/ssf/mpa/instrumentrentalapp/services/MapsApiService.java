package vttp.ssf.mpa.instrumentrentalapp.services;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.ssf.mpa.instrumentrentalapp.models.RentalListing;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.Location;

// service for calling maps api

@Service
public class MapsApiService {

    // for debugging/tracking
    private Logger logger = Logger.getLogger(MapsApiService.class.getName());
    
    // value injections (API KEY TO BE SET IN ENV)
    @Value("${google.api.key}")
    private String GOOGLE_API_KEY;

    @Value("${google.geocoding.api.url}")
    private String GEOCODING_API_URL;

    @Value("${google.staticmap.api.url}")
    private String STATICMAP_API_URL;

    // resttemplate
    private final RestTemplate restTemplate = new RestTemplate();

    // convert user address to lat/long using geocoding api - separate geocoding api from static maps api to store for redis usage
    public Location getLocation(String address) throws UnsupportedEncodingException, RestClientException {

        try {

            // URL-encode the address
            String encodedAddress = URLEncoder.encode(address, "UTF-8");

            // build request url
            String reqUrl = UriComponentsBuilder
                    .fromUriString(GEOCODING_API_URL)
                    .queryParam("address", encodedAddress)
                    .queryParam("key", GOOGLE_API_KEY)
                    .toUriString();

            // logger
            logger.info(">>> Geocoding request URL built: %s".formatted(reqUrl));

            try {

                // get responseentity from api
                ResponseEntity<String> resp = restTemplate.getForEntity(reqUrl, String.class);

                // get resp string and read to jsonobject
                String respString = resp.getBody();
                JsonReader jReader = Json.createReader(new StringReader(respString));
                JsonObject respJObject = jReader.readObject();

                if (respJObject.containsKey("results") && !respJObject.getJsonArray("results").isEmpty()) {

                    // read results object nested in results array
                    JsonObject resultsJObject = respJObject.getJsonArray("results").getJsonObject(0);

                    // read location object nested in geometry object
                    JsonObject locJObject = resultsJObject.getJsonObject("geometry").getJsonObject("location");

                    // get lat/long
                    double latitude = locJObject.getJsonNumber("lat").doubleValue();
                    double longitude = locJObject.getJsonNumber("lng").doubleValue();

                    // read long_name (region) nested in address comp array
                    JsonArray addJArray = resultsJObject.getJsonArray("address_components");

                    // neighborhood to hold default string - in case results don't contain neighborhood
                    String neighborhood = "No neighbourhood found";

                    // loop through the address components - varies for every location
                    for (int i = 0; i < addJArray.size(); i++) {

                        // get each component and 
                        JsonObject component = addJArray.getJsonObject(i);
                        JsonArray types = component.getJsonArray("types");
                        
                        // check if this component is of type 'neighborhood'
                        if (types.getString(0).equals("neighborhood")) {

                             // Check if 'long_name' exists and is not null
                            if (component.containsKey("long_name")) {
                                neighborhood = component.getString("long_name"); // Assign neighborhood name
                            }

                            break; // break once found

                        }
                    }

                    // put in location obj
                    Location location = new Location(latitude, longitude, neighborhood);

                    // logger
                    logger.info(">>> Geocoding response received: %s".formatted(location.toString()));

                    // return location obj
                    return location;

                } else {
                    // logger
                    logger.severe(">>> No geolocation found for given address: %s".formatted(address));
                    throw new RuntimeException("No results found for the given address"); // throw runtime exception if no results 
                }

            } catch (RestClientException e) {
                // logger
                logger.severe(">>> Error occurred during geocoding request: %s".formatted(e.getMessage()));
                throw e; // let controller handle
            }
            
        } catch (UnsupportedEncodingException e) {
            // logger
            logger.severe(">>> Error occurred during geocoding request: %s".formatted(e.getMessage()));
            throw e; // let controller handle
        }

    }
    
    // get static map using static maps api - return URI to use as img src
    public String getStaticMap(RentalListing listing) {

        // get lat/long from listing
        double latitude = listing.getLatitude();
        double longitude = listing.getLongitude();

        // build uri
        String reqUrl = UriComponentsBuilder
            .fromUriString(STATICMAP_API_URL)
            .queryParam("center", String.format("%s,%s", latitude, longitude))
            .queryParam("format", "jpg") // set jpg format
            .queryParam("zoom", 13) // set zoom level
            .queryParam("size", "180x180") // set image size
            .queryParam("scale", 2)
            .queryParam("markers", String.format("%s,%s", latitude, longitude))
            .queryParam("key", GOOGLE_API_KEY)
            .toUriString();

        // logging
        logger.info("StaticMaps request URL build: %s".formatted(reqUrl.toString()));
    
        return reqUrl;

    }

}
