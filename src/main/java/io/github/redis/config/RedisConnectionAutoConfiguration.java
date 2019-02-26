package io.github.redis.config;
/*
 * created by pareshP on 20/02/19
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redis.client.RedisClient;
import io.github.redis.helper.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnClass({ JedisConnection.class, RedisOperations.class, Jedis.class })
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConnectionAutoConfiguration {

    private RedisProperties redisProperties;

    private boolean sentinelEnabled;

    private boolean clusterEnabled;

    public RedisConnectionAutoConfiguration(@Autowired RedisProperties redisProperties,
                                            @Value("${spring.redis.sentinel.enabled}") boolean sentinelEnabled,
                                            @Value("${spring.redis.cluster.enabled}") boolean clusterEnabled) {
        this.redisProperties = redisProperties;
        this.sentinelEnabled = sentinelEnabled;
        this.clusterEnabled = clusterEnabled;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory() {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();

        if (pool != null) {
            poolConfig.setMaxTotal(pool.getMaxActive());
            poolConfig.setMinIdle(pool.getMinIdle());
            poolConfig.setMaxIdle(pool.getMaxIdle());
        } else {
            poolConfig.setMaxTotal(Constants.DEFAULT_MAX_TOTAL);
            poolConfig.setMinIdle(Constants.DEFAULT_MIN_IDLE);
            poolConfig.setMaxIdle(Constants.DEFAULT_MAX_IDLE);
        }

        if (sentinelEnabled) {
            return sentinelConnectionFactory(poolConfig);
        } else if (clusterEnabled) {
            return clusterConnectionFactory(poolConfig);
        } else {
            return standaloneConnectionFactory();
        }
    }

    private RedisConnectionFactory standaloneConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setDatabase(redisProperties.getDatabase());
        standaloneConfiguration.setHostName(redisProperties.getHost());
        standaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        standaloneConfiguration.setPort(redisProperties.getPort());

        return new JedisConnectionFactory(standaloneConfiguration);
    }

    private RedisConnectionFactory clusterConnectionFactory(JedisPoolConfig poolConfig) {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();

        redisProperties.getCluster().getNodes().stream()
                .map(node -> node.split(":"))
                .forEach(hostPort -> clusterConfiguration
                        .addClusterNode(new RedisNode(hostPort[0], Integer.parseInt(hostPort[1]))));

        clusterConfiguration.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
        clusterConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

        return new JedisConnectionFactory(clusterConfiguration, poolConfig);
    }

    private RedisConnectionFactory sentinelConnectionFactory(JedisPoolConfig poolConfig) {
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        sentinelConfiguration.setMaster(redisProperties.getSentinel().getMaster());
        sentinelConfiguration.setDatabase(redisProperties.getDatabase());
        sentinelConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

        redisProperties.getSentinel().getNodes().stream()
                .map(node -> node.split(":"))
                .forEach(hostPort -> sentinelConfiguration.sentinel(hostPort[0], Integer.parseInt(hostPort[1])));

        return new JedisConnectionFactory(sentinelConfiguration, poolConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    @Qualifier("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    @Qualifier("stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final StringRedisTemplate template = new StringRedisTemplate();

        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    @Qualifier("redisClient")
    public RedisClient redisClient() {
        return new RedisClient(
                redisTemplate(redisConnectionFactory()),
                stringRedisTemplate(redisConnectionFactory()));
    }
}
