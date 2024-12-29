package vttp.ssf.mpa.instrumentrentalapp.configs;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import vttp.ssf.mpa.instrumentrentalapp.utils.RedisConstants;

@Configuration
@EnableCaching
public class RedisConfig {

   private final Logger logger = Logger.getLogger(RedisConfig.class.getName());

   // Get all the redis configuration into the class
   @Value("${spring.data.redis.host}")
   private String redisHost;

   @Value("${spring.data.redis.port}")
   private int redisPort;

   @Value("${spring.data.redis.database}")
   private int redisDatabase;

   @Value("${spring.data.redis.username}")
   private String redisUsername;

   @Value("${spring.data.redis.password}")
   private String redisPassword;

   @Bean(RedisConstants.REDIS_STRING)
   public RedisTemplate<String, String> createRedisTemplateObject() {
      
      // Create a database configuration
      RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
      // Sets the database - select 0
      config.setDatabase(redisDatabase);
      // Set the username and password if they are set
      if (!redisUsername.trim().equals("")) {
         logger.info("Setting Redis username and password");
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
      }

      // Create a connection to the database
      JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();

      // Create a factory to connect to Redis
      JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
      jedisFac.afterPropertiesSet();

      // Create the RedisTemplate
      RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(jedisFac);
      redisTemplate.setKeySerializer(new StringRedisSerializer());
      redisTemplate.setValueSerializer(new StringRedisSerializer());
      redisTemplate.setHashKeySerializer(new StringRedisSerializer());
      return redisTemplate;
   }

   @Bean(RedisConstants.REDIS_OBJECT)
   public RedisTemplate<String, Object> createRedisTemplate() {
      
      // Create a database configuration
      RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
      // Sets the database - select 0
      config.setDatabase(redisDatabase);
      // Set the username and password if they are set
      if (!redisUsername.trim().equals("")) {
         logger.info("Setting Redis username and password");
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
      }

      // Create a connection to the database
      JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();

      // Create a factory to connect to Redis
      JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
      jedisFac.afterPropertiesSet();

      // Create the RedisTemplate
      RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(jedisFac);
      redisTemplate.setKeySerializer(new StringRedisSerializer());
      redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
      redisTemplate.setHashKeySerializer(new StringRedisSerializer());
      redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
      return redisTemplate;
   }

}
