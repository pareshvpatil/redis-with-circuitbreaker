package org.foobar.redis.helper;
/*
 * created by pareshP on 20/02/19
 */

public class Constants {

    public static final String REDIS_GET_COMMAND = "redisClientGet";
    public static final String REDIS_SET_COMMAND = "redisClientSet";
    public static final String REDIS_DELETE_COMMAND = "redisClientDelete";

    public static final String REDIS_GET_FROM_DB = "redisGetFromDB";
    public static final String REDIS_PUT_IN_DB = "redisPutInDB";
    public static final int DEFAULT_MAX_TOTAL = 8;
    public static final int DEFAULT_MIN_IDLE = 0;
    public static final int DEFAULT_MAX_IDLE = 8;

    private Constants() {
    }
}
