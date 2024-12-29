package vttp.ssf.mpa.instrumentrentalapp.repositories;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.ssf.mpa.instrumentrentalapp.utils.RedisConstants;

// for storing/retrieving rental listings

@Repository
public class RentRepository {

    @Autowired @Qualifier(RedisConstants.REDIS_STRING)
    private RedisTemplate<String, String> redisTemplate;

    // LISTING FUNCTIONS - saving as jsonobj due to complex model

    // save rental listing in redis
    // redis-cli command
    // hset listings <listingId> <jsonstring>
    public void saveListing(String listingId, String listingString) {
        redisTemplate.opsForHash().put("listings", listingId, listingString);
    }

    // get rental listings from redis
    // redis-cli command
    // hgetall listings
    public Map<Object, Object> getAllListings() {
        return redisTemplate.opsForHash().entries("listings");
    }

    // get rental listing from redis
    // redis-cli command
    // hget listings <listingId>
    public String getListing(String listingId) {
        return (String) redisTemplate.opsForHash().get("listings", listingId);
    } 

    // delete rental listing from redis
    // redis-cli command
    // hdel listings <listingId>
    public void deleteListing(String listingId) {
        redisTemplate.opsForHash().delete("listings", listingId);
    }

    // USER LISTING COUNT FUNCTIONS

    // save listing ids under user in redis
    // redis-cli command
    // sadd user:<username>:listings <listId>
    public void saveListingId(String username, String listingId) {
        String userKey = String.format("user:%s:listings", username);
        redisTemplate.opsForSet().add(userKey, listingId);
    }

    // get size of listing ids under user in redis
    // redis-cli command
    // scard user:<username>:listings <listId>
    public long getListingIdCount(String username) {
        return redisTemplate.opsForSet().size(String.format("user:%s:listings", username));
    }

    // del listing id from user
    // redis-cli command
    // srem user:<username>:listings <listId>
    public void delListingId(String username, String listingId) {
        String userKey = String.format("user:%s:listings", username);
        redisTemplate.opsForSet().remove(userKey, listingId);
    }

    // LIKE COUNT FUNCTIONS (TO DISPLAY LISTING'S LIKES) - using opsforset() to ensure uniqueness of each like

    // check if listing alr liked
    // redis-cli command
    // sismember listing:<listingId>:likes <username>
    public boolean isLikedList(String listingId, String username) {
        return redisTemplate.opsForSet().isMember(String.format("listing:%s:likes", listingId), username);
    }

    // save liked listing count - ensure each user can only like the listing once
    // redis-cli command
    // sadd listing:<listingId>:likes <username>
    public void saveListingLike(String listingId, String username) {
        redisTemplate.opsForSet().add(String.format("listing:%s:likes", listingId), username);
    }

    // retrieve liked listing count
    // redis-cli command
    // scard listing:<listingId>:likes
    public long getListingLikes(String listingId) {
        return redisTemplate.opsForSet().size(String.format("listing:%s:likes",listingId));
    }

    // delete like from listing count
    // redis-cli command
    // srem listing:<listingId>:likes <username>
    public void delListingLike(String listingId, String username) {
        redisTemplate.opsForSet().remove(String.format("listing:%s:likes", listingId), username);
    }

    // LIKE LISTING FUNCTIONS (TO DISPLAY USER'S LIKED LISTINGS) - using opsforset() to ensure uniqueness of each like

    // check if listing alr liked by user
    // redis-cli command
    // sismember user:<username>:likes <listingId>
    public boolean isLikedUser(String username, String listingId) {
        return redisTemplate.opsForSet().isMember(String.format("user:%s:likes", username), listingId);
    }

    // save listing id as liked under username
    // redis-cli command
    // sadd user:<username>:likes <listingId>
    public void saveUserLike(String username, String listingId) {
        redisTemplate.opsForSet().add(String.format("user:%s:likes", username), listingId);
    }

    // retrieve listing ids that user liked
    // redis-cli command
    // smembers user:<username>:likes
    public Set<String> getUserLikes(String username) {
        return redisTemplate.opsForSet().members(String.format("user:%s:likes", username));
    }

    // delete liked listing for user
    // redis-cli command
    // srem user:<username>:likes <listingId>
    public void delUserLike(String username, String listingId) {
        redisTemplate.opsForSet().remove(String.format("user:%s:likes", username), listingId);
    }

    // CACHING FUNCTIONS

    // cache query - set expiry 5 mins
    // redis-cli command
    // hset <cacheKey> <listingId> <listingString>
    // expire <cacheKey> <seconds>
    public void cacheQuery(String cacheKey, String listingId, String listingString) {
        redisTemplate.opsForHash().put(cacheKey, listingId, listingString);
        redisTemplate.expire(cacheKey, 5 * 60, TimeUnit.SECONDS);
    }

    // get cached queries - will return Map<listingId, listingString>
    // redis-cli command
    // hgetall <cacheKey>
    public Map<Object, Object> getAllCachedQueries(String cacheKey) {
        return redisTemplate.opsForHash().entries(cacheKey);
    }

    // delete cached query
    // redis-cli command
    // del <cacheKey>
    public void delQuery(String cacheKey) {
        redisTemplate.delete(cacheKey);
    }

    // to check health of app - check if redis has random key
    // redis-cli command
    // randomkey
	public String randomKey() {
		return redisTemplate.randomKey();
	}

}
