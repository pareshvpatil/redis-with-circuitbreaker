package io.github.redis.config;

import io.github.redis.helper.Constants;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class RedisConnectionAutoConfigurationTest {

    @Tested
    private RedisConnectionAutoConfiguration autoConfiguration;

    @Injectable
    private RedisProperties redisProperties;

    @Injectable
    private boolean sentinelEnabled = false;

    @Injectable
    private boolean clusterEnabled = false;

    @Before
    public void setUp() {
        redisProperties = new RedisProperties();
        redisProperties.setPassword("");
        redisProperties.setPort(6379);
        redisProperties.setHost("localhost");

        RedisProperties.Pool pool = new RedisProperties.Pool();
        pool.setMaxIdle(1);
        pool.setMinIdle(1);
        pool.setMaxActive(8);
        pool.setMaxWait(Duration.ZERO);

        redisProperties.getJedis().setPool(pool);

        RedisProperties.Cluster cluster = new RedisProperties.Cluster();
        cluster.setNodes(Arrays.asList("localhost:30001","localhost:30002","localhost:30003"));
        cluster.setMaxRedirects(2);

        redisProperties.setCluster(cluster);

        RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
        sentinel.setMaster("my-master");
        sentinel.setNodes(Arrays.asList("localhost:30001","localhost:30002","localhost:30003"));

        redisProperties.setSentinel(sentinel);

        autoConfiguration = new RedisConnectionAutoConfiguration(redisProperties,sentinelEnabled,clusterEnabled);
    }

    @Test
    public void testRedisConnectionFactoryShouldInstantiateClusterInstanceIfClusterEnabled() {
        autoConfiguration = new RedisConnectionAutoConfiguration(redisProperties,false,true);

        RedisConnectionFactory factory = autoConfiguration.redisConnectionFactory();

        assertNotNull(factory);
        assertNotNull(((JedisConnectionFactory)factory).getClusterConfiguration());
        assertNull(((JedisConnectionFactory)factory).getSentinelConfiguration());
    }

    @Test
    public void testRedisConnectionFactoryShouldInstantiateSentinelInstanceIfSentinelEnabled() {
        autoConfiguration = new RedisConnectionAutoConfiguration(redisProperties,true,false);

        RedisConnectionFactory factory = autoConfiguration.redisConnectionFactory();

        assertNotNull(factory);
        assertNotNull(((JedisConnectionFactory)factory).getSentinelConfiguration());
        assertNull(((JedisConnectionFactory)factory).getClusterConfiguration());
    }

    @Test
    public void testRedisConnectionFactoryShouldInstantiateStandaloneInstanceIfBothDisabled() {
        autoConfiguration = new RedisConnectionAutoConfiguration(redisProperties,false,false);

        RedisConnectionFactory factory = autoConfiguration.redisConnectionFactory();

        assertNotNull(factory);
        assertNotNull(((JedisConnectionFactory) factory).getStandaloneConfiguration());
        assertNull(((JedisConnectionFactory)factory).getSentinelConfiguration());
        assertNull(((JedisConnectionFactory)factory).getClusterConfiguration());
    }

    @Test
    public void testRedisConnectionFactoryShouldInstantiateStandaloneInstanceWithDefaultPoolConfigs() {

        redisProperties.getJedis().setPool(null);

        autoConfiguration = new RedisConnectionAutoConfiguration(redisProperties,false,false);

        RedisConnectionFactory factory = autoConfiguration.redisConnectionFactory();

        assertNotNull(factory);
        assertNotNull(((JedisConnectionFactory) factory).getStandaloneConfiguration());
        assertNotNull(((JedisConnectionFactory) factory).getPoolConfig());
        assertEquals(Constants.DEFAULT_MAX_IDLE, ((JedisConnectionFactory) factory).getPoolConfig().getMaxIdle());
        assertNull(((JedisConnectionFactory)factory).getSentinelConfiguration());
        assertNull(((JedisConnectionFactory)factory).getClusterConfiguration());
    }

    @Test
    public void testRedisTemplateShouldInstantiate() {
        RedisTemplate<String, Object> redisTemplate = autoConfiguration
                .redisTemplate(autoConfiguration.redisConnectionFactory());

        assertNotNull(redisTemplate);
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }

    @Test
    public void testStringRedisTemplateShouldInstantiate() {
        StringRedisTemplate redisTemplate = autoConfiguration
                .stringRedisTemplate(autoConfiguration.redisConnectionFactory());

        assertNotNull(redisTemplate);
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }

    @Test
    public void testRedisClientShouldInstantiate() {
        assertNotNull(autoConfiguration.redisClient());
    }
}