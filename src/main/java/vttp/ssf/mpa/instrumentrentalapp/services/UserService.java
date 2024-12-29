package vttp.ssf.mpa.instrumentrentalapp.services;

import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.ssf.mpa.instrumentrentalapp.models.UserLogin;
import vttp.ssf.mpa.instrumentrentalapp.models.UserProfile;
import vttp.ssf.mpa.instrumentrentalapp.models.UserRegistration;
import vttp.ssf.mpa.instrumentrentalapp.models.helpers.Location;
import vttp.ssf.mpa.instrumentrentalapp.repositories.UserRepository;

// service for user login/regis/profile - everything related to user that is not core function of app (rentals/listings)

@Service
public class UserService {

    // for debugging/tracking
    private Logger logger = Logger.getLogger(UserService.class.getName());

    // for api calling
    @Autowired
    private MapsApiService mapsSvc;

    // for redis
    @Autowired
    private UserRepository userRepo;

    // PW HASHING METHODS

    // method to hash pw
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // method to check if pw matches
    public boolean validate(String password, String storedPassword) {
        return BCrypt.checkpw(password, storedPassword);
    }

    // USER REGISTRATION METHODS

    // save new user details to redis
    public void saveUser(UserRegistration user) throws Exception {

        // get number - remove country code and set plain number if applicable
        String number = user.getNumber();
        if (number.startsWith("+65")) {
            user.setNumber(number.substring(3));
        }

        // get user address from regis
        String address = user.getAddress();

        // get location obj from mapssvc
        Location location = mapsSvc.getLocation(address);
        
        // get lat/long/nbhd from address
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String neighborhood = location.getNeighborhood();

        // get password from regis
        String userPassword = user.getPassword();

        // get username from regis
        String username = user.getUsername();

        // hash password for storage
        String hashPassword = hash(userPassword);

        // create jsonobject of user
        JsonObject userJObject = Json.createObjectBuilder()
            .add("number", user.getNumber()) // to pass into listing / validation purposes
            .add("latitude", latitude) // to pass into listing / get nearby listings
            .add("longitude", longitude) // to pass into listing / get nearby listings
            .add("neighborhood", neighborhood) // to pass into listing - stored here to rely on db instead of calling api multiple times
            .add("birthDate", user.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) // store in long (epoch)
            .add("username", username) // for display / binding to listings as ownerName
            .add("hashPassword", hashPassword) // stored securely
            .build();

        // jsonobject to string
        String userJString = userJObject.toString();

        try {
            // save in redis
            userRepo.saveUser(username, userJString);

            // log for tracking
            logger.info("User %s was saved successfully".formatted(username));

        } catch (Exception e) {

            throw e; // let controller handle

        }
        
    }

    // USER LOGIN METHODS

    // retrieve user object from redis by username
    public UserRegistration retrieveUser(String username) {

        // get user as jstring from redis
        String userJString = userRepo.retrieveUser(username);

        // read jstring
        try (JsonReader jReader = Json.createReader(new StringReader(userJString))) {

            // convert to jobject
            JsonObject userJObject = jReader.readObject();

            // map to user object
            UserRegistration user = new UserRegistration(
                userJObject.getString("number"), 
                userJObject.getJsonNumber("latitude").doubleValue(), 
                userJObject.getJsonNumber("longitude").doubleValue(),
                userJObject.getString("neighborhood"), 
                Instant.ofEpochMilli(userJObject.getJsonNumber("birthDate").longValue()).atZone(ZoneId.systemDefault()).toLocalDate(), // convert to localdate
                userJObject.getString("username"), 
                userJObject.getString("hashPassword"));

            // log for tracking
            logger.info("User %s was retrieved successfully".formatted(username));

            return user;

        } catch (Exception e) {

            throw e; // let controller handle

        }

    }

    // USER VALIDATION METHODS

    // check if user exists in redis
    public boolean userExists(String username) {
        return userRepo.userExists(username);
    }

    // validate username and password for login
    public boolean validate(UserLogin logUser) {

        // get login details
        String username = logUser.getUsername();
        String password = logUser.getPassword();

        // if user exists in redis
        if (userExists(username)) {

            // get user from redis
            UserRegistration regUser = retrieveUser(username);

            // get hashed password from user
            String hashPassword = regUser.getPassword();

            // validate user
            boolean isValidated = validate(password, hashPassword);

            // log for tracking
            logger.info("User %s validation: %s".formatted(username, isValidated));

            return isValidated;

        } 

        // log for debugging
        logger.warning("User %s not found in Redis".formatted(username));

        // if user doesn't exist in redis or password doesn't match
        return false;

    }

    // USER PROFILE METHODS
    // save profile
    public void saveProfile(String username, UserProfile profile) {
        if (profile != null) {
            userRepo.saveProfile(username, profile);
        }
    }

    // retrieve profile
    public UserProfile getProfile(String username) {

        // get prof object from userrepo
        Map<Object, Object> profMap = userRepo.getProfile(username);

        // new prof to store values
        UserProfile profile = new UserProfile();

        // add values to profile
        for (Map.Entry<Object, Object> entry : profMap.entrySet()) {
            if (entry.getKey().equals("username")) { profile.setUsername((String) entry.getValue()); }
            if (entry.getKey().equals("nickname")) { profile.setNickname((String) entry.getValue()); }
            if (entry.getKey().equals("gender")) { profile.setGender((String) entry.getValue()); }
            if (entry.getKey().equals("profilePicture")) { profile.setProfilePicture((String) entry.getValue()); }
            if (entry.getKey().equals("bio")) { profile.setBio((String) entry.getValue()); }
            if (entry.getKey().equals("dealPreferences")) { profile.setDealPreferences((String) entry.getValue()); }
        }

        // return profile - will return new empty profile if no values set yet
        return profile;
    }

}
