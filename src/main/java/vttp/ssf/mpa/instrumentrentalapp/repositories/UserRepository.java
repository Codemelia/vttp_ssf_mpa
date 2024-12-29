package vttp.ssf.mpa.instrumentrentalapp.repositories;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.ssf.mpa.instrumentrentalapp.models.UserProfile;
import vttp.ssf.mpa.instrumentrentalapp.utils.RedisConstants;

// for user regis/login/profiles - everything related to user that is not core function of app (rentals/listings)

@Repository
public class UserRepository {

    // redistemplate
    @Autowired @Qualifier(RedisConstants.REDIS_OBJECT)
    private RedisTemplate<String, Object> redisTemplate;
    
    // for saving user as json string under username - saving as jsonobject as won't be accessed frequently
    // redis-cli command
    // hset users <username> <userString>
    public void saveUser(String username, String userString) {
        redisTemplate.opsForHash().put("users", username, userString);
    }

    // for retrieving user as json string under user id
    // redis-cli command
    // hget users <username>
    public String retrieveUser(String username) {
        return (String) redisTemplate.opsForHash().get("users", username);
    }

    // for checking if user alr exists in redis
    public boolean userExists(String username) {
        return redisTemplate.opsForHash().hasKey("users", username);
    }

    // for saving userprofile - not using jsonobj since few fields & will be accessed frequently
    // redis-cli command
    // hset profile:<username> <profilefieldkey> <profilefieldvalue>
    public void saveProfile(String username, UserProfile profile) {
        String key = String.format("user:%s:profile", username);
        Map<String, String> profileMap = new HashMap<>();
        profileMap.put("username", username);
        profileMap.put("nickname", profile.getNickname());
        profileMap.put("gender", profile.getGender());

        // set default pic for prof pic
        String profilePic = profile.getProfilePicture();
        profileMap.put("profilePicture", profilePic);
        profileMap.put("bio", profile.getBio());
        profileMap.put("dealPreferences", profile.getDealPreferences());
        redisTemplate.opsForHash().putAll(key, profileMap);
    }

    // for retrieving user profile
    public Map<Object, Object> getProfile(String username) {
        String key = String.format("user:%s:profile", username);
        return redisTemplate.opsForHash().entries(key);
    }

}
