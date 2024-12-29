package vttp.ssf.mpa.instrumentrentalapp.repositories;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.ssf.mpa.instrumentrentalapp.utils.RedisConstants;

// for storing/retrieving calendar bookings - storing as json for ease of api integration if needed

@Repository
public class CalendarRepository {

    @Autowired @Qualifier(RedisConstants.REDIS_STRING)
    private RedisTemplate<String, String> redisTemplate; 

    // save booking under user
    // redis-cli command
    // hset <username>:calendar <bookingId> <jsonstring>
    public void saveBooking(String username, String bookingId, String jString) {
        String userKey = "user:%s:calendar".formatted(username);
        redisTemplate.opsForHash().put(userKey, bookingId, jString);
    }

    // retrieve booking as jsonstring
    // redis-cli command
    // hget <username>:calendar <bookingId>
    public String getBooking(String username, String bookingId) {
        String userKey = "user:%s:calendar".formatted(username);
        return (String) redisTemplate.opsForHash().get(userKey, bookingId);
    }

    // del a booking from Redis
    // redis-cli command
    // hdel <username>:calendar <bookingId>
    public void delBooking(String username, String bookingId) {
        String userKey = "user:%s:calendar".formatted(username);
        redisTemplate.opsForHash().delete(userKey, bookingId);
    }

    // get all bookings as Map<bookingId, bookingjsonstring>
    // redis-cli command
    // hgetall <username>:calendar
    public Map<Object, Object> getAllBookings(String username) {
        String userKey = "user:%s:calendar".formatted(username);
        return redisTemplate.opsForHash().entries(userKey);
    }

}
