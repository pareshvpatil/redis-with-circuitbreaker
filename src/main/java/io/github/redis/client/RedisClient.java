package io.github.redis.client;
/*
 * created by pareshP on 20/02/19
 */

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.github.redis.helper.Constants.*;

/**
 * Redis client Bean. redis interaction with the use of {@link RedisTemplate}
 * and {@link StringRedisTemplate} with help of {@link HashOperations} for hash operations.
 * Wrapped with {@link HystrixCommand} for each redis operation.
 */
@Component
public class RedisClient {

    private static final Logger LOGGER = LogManager.getLogger(RedisClient.class.getName());

    private RedisTemplate<String, Object> redisTemplate;

    private StringRedisTemplate stringRedisTemplate;

    private HashOperations<String, Object, Object> hashOperations;

    /**
     * Instantiates a new Redis client. All Args Constructor.
     *
     * @param redisTemplate       the redis template
     * @param stringRedisTemplate the string redis template
     */
    public RedisClient(RedisTemplate<String, Object> redisTemplate,
                       StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * Sets element in redis.
     *
     * @param <T>     the type parameter
     * @param key     the key
     * @param element the element
     */
    @HystrixCommand(
            commandKey = REDIS_SET_COMMAND,
            groupKey = REDIS_SET_COMMAND,
            threadPoolKey = REDIS_SET_COMMAND,
            fallbackMethod = "fallbackForSetElementInRedis"
    )
    public <T> void setElementInRedis(String key, T element) {
        LOGGER.info("Set Called for Key:{}", key);
        if (element instanceof String) {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(element));
        } else {
            redisTemplate.opsForValue().set(key, element);
        }
    }

    /**
     * Sets element in redis with expiry.
     *
     * @param <T>             the type parameter
     * @param key             the key
     * @param element         the element
     * @param expiryInSeconds the expiry in seconds
     */
    public <T> void setElementInRedisWithExpiry(String key, T element, int expiryInSeconds) {
        LOGGER.info("Set Called for Key:{}, expiry(second):{}", key, expiryInSeconds);
        if (element instanceof String) {
            stringRedisTemplate.opsForValue().set(key, String.valueOf(element), expiryInSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, element, expiryInSeconds, TimeUnit.SECONDS);
        }
    }

    private <T> void fallbackForSetElementInRedis(String key, T element) {
        LOGGER.error("Redis SET Failed for key:{}, element:{}", key, element);
    }

    /**
     * Gets element from redis.
     *
     * @param <T>    the type parameter
     * @param key    the key
     * @param tClass the t class (this is return type of the element in redis against the key)
     * @return the element from redis
     */
    @HystrixCommand(commandKey = REDIS_GET_COMMAND,
            groupKey = REDIS_GET_COMMAND,
            threadPoolKey = REDIS_GET_COMMAND,
            fallbackMethod = "fallbackForGetElementFromRedis"
    )
    public <T> T getElementFromRedis(String key, Class<T> tClass) {
        LOGGER.info("Get Called for Key:{}, ResponseType:{}", key, tClass.getName());
        if (tClass == String.class) {
            String obj = stringRedisTemplate.opsForValue().get(key);

            LOGGER.info("Get Called for Key:{}, ResponseType:{}, Response:{}",
                    key, tClass.getName(), String.valueOf(obj));

            return tClass.cast(obj);
        } else {
            T element = tClass.cast(redisTemplate.opsForValue().get(key));

            LOGGER.info("Get Called for Key:{}, ResponseType:{}, Response:{}", key, tClass.getName(), element);

            return tClass.cast(element);
        }
    }

    private <T> T fallbackForGetElementFromRedis(String key, Class<T> tClass) {
        LOGGER.error("Redis Get Failed for key:{}, type:{}, returning NULL", key, tClass.getName());
        return null;
    }

    /**
     * Delete element from redis.
     *
     * @param key the key
     */
    @HystrixCommand(
            commandKey = REDIS_DELETE_COMMAND,
            groupKey = REDIS_DELETE_COMMAND,
            threadPoolKey = REDIS_DELETE_COMMAND,
            fallbackMethod = "fallbackForDeleteElementFromRedis"
    )
    public void deleteElementFromRedis(String key) {
        redisTemplate.delete(key);
    }

    private void fallbackForDeleteElementFromRedis(String key) {
        LOGGER.error("Redis Delete Failed for key:{}", key);
    }

    /**
     * Put in redis using hash ops.
     *
     * @param <T>       the type parameter
     * @param key       the key
     * @param hashKey   the hash key
     * @param hashValue the hash value
     */
    @HystrixCommand(
            commandKey = REDIS_PUT_IN_DB,
            groupKey = REDIS_PUT_IN_DB,
            threadPoolKey = REDIS_PUT_IN_DB,
            fallbackMethod = "fallbackForPutInDBUsingHashOps"
    )
    public <T> void putInRedisUsingHashOps(String key, Object hashKey, T hashValue) {
        if (hashValue != null && hashKey != null) {
            hashOperations.put(key, hashKey, hashValue);
        }
    }

    private <T> void fallbackForPutInDBUsingHashOps(String key, Object hashKey, T hashValue) {
        LOGGER.error("Redis Put Using HashOps Failed for key:{}, hashKey:{}, hashValue:{}", key, hashKey, hashValue);
    }

    /**
     * Gets from redis using hash ops.
     *
     * @param <T>     the type parameter
     * @param key     the key
     * @param hashKey the hash key
     * @param tClass  the t class (this is return type of the element in redis against the key)
     * @return the from redis using hash ops
     */
    @HystrixCommand(
            commandKey = REDIS_GET_FROM_DB,
            groupKey = REDIS_GET_FROM_DB,
            threadPoolKey = REDIS_GET_FROM_DB,
            fallbackMethod = "fallbackForGetFromDBUsingHashOps"
    )
    public <T> T getFromRedisUsingHashOps(String key, Object hashKey, Class<T> tClass) {
        if (hashKey != null) {
            return tClass.cast(hashOperations.get(key, hashKey));
        }
        return null;
    }

    private <T> T fallbackForGetFromDBUsingHashOps(String key, Object hashKey, Class<T> tClass) {
        LOGGER.error("Redis Get Using HashOps Failed for key:{}, hashKey:{} return type:{} returning NULL", key, hashKey, tClass);
        return null;
    }

    /**
     * Gets keys from pattern.
     *
     * @param pattern the pattern
     * @return the keys from pattern
     */
    @HystrixCommand(
            commandKey = REDIS_GET_FROM_DB,
            groupKey = REDIS_GET_FROM_DB,
            threadPoolKey = REDIS_GET_FROM_DB,
            fallbackMethod = "fallbackForGetKeysFromPattern"
    )
    public Set<String> getKeysFromPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }

    private Set<String> fallbackForGetKeysFromPattern(String pattern) {
        LOGGER.error("Fallback for get keys from pattern executed, returning empty set, pattern:{}", pattern);
        return Collections.emptySet();
    }
}
