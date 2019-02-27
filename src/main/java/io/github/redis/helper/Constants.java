package io.github.redis.helper;
/*
 * created by pareshP on 20/02/19
 */

/**
 * The type Constants.
 */
public class Constants {

    /**
     * The constant REDIS_GET_COMMAND. This is Hystrix command name for GET command of redis.
     */
    public static final String REDIS_GET_COMMAND = "redisClientGet";
    /**
     * The constant REDIS_SET_COMMAND. This is Hystrix command name for SET command of redis.
     */
    public static final String REDIS_SET_COMMAND = "redisClientSet";
    /**
     * The constant REDIS_DELETE_COMMAND. This is Hystrix command name for DELETE command of redis.
     */
    public static final String REDIS_DELETE_COMMAND = "redisClientDelete";

    /**
     * The constant REDIS_GET_FROM_DB. This is Hystrix command name for HGETALL command of redis.
     */
    public static final String REDIS_GET_FROM_DB = "redisGetFromDB";
    /**
     * The constant REDIS_PUT_IN_DB. This is Hystrix command name for SET command of redis.
     */
    public static final String REDIS_PUT_IN_DB = "redisPutInDB";
    /**
     * The constant DEFAULT_MAX_TOTAL. This is default pool config property.
     */
    public static final int DEFAULT_MAX_TOTAL = 8;
    /**
     * The constant DEFAULT_MIN_IDLE. This is default pool config property.
     */
    public static final int DEFAULT_MIN_IDLE = 0;
    /**
     * The constant DEFAULT_MAX_IDLE. This is default pool config property.
     */
    public static final int DEFAULT_MAX_IDLE = 8;

    private Constants() {
    }
}
