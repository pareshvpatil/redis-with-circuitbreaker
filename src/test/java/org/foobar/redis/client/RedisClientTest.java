package org.foobar.redis.client;

import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JMockit.class)
public class RedisClientTest {

    @Tested
    private RedisClient redisClient;

    @Injectable
    private RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class, RETURNS_DEEP_STUBS);

    @Injectable
    private StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);

    @Mocked
    private ValueOperations stringValueOps = mock(ValueOperations.class, RETURNS_DEEP_STUBS);

    @Mocked
    private ValueOperations valueOps = mock(ValueOperations.class, RETURNS_DEEP_STUBS);

    @Mocked
    private HashOperations hashOps = mock(HashOperations.class, RETURNS_DEEP_STUBS);

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() throws Exception {
        redisClient = new RedisClient(redisTemplate, stringRedisTemplate);
        stringValueOps = stringRedisTemplate.opsForValue();
        valueOps = redisTemplate.opsForValue();
        hashOps = redisTemplate.opsForHash();

        initiateStreams();
    }

    private void initiateStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testSetElementInRedisShouldPutInRedis() {
        redisClient.setElementInRedis("key", "value");
        redisClient.setElementInRedisWithExpiry("key", "value", 3600);
    }

    @Test
    public void getElementFromRedis() {
        when(stringValueOps.get("key")).thenReturn("value");
        assertEquals("value", redisClient.getElementFromRedis("key", String.class));
    }

    @Test
    public void getElementFromRedisObject() {
        when(valueOps.get("key")).thenReturn("value");
        assertEquals("value", (String) redisClient.getElementFromRedis("key", Object.class));
    }

    @Test
    public void deleteElementFromRedis() {
        redisClient.deleteElementFromRedis("key");
    }

    @Test
    public void putInRedisUsingHashOps() {
        redisClient.putInRedisUsingHashOps("key", "hashKey", "value");
    }

    @Test
    public void getFromRedisUsingHashOps() {
        when(hashOps.get("key", "hashKey")).thenReturn("value");
        assertEquals("value", redisClient.getFromRedisUsingHashOps("key", "hashKey", String.class));
    }

    @Test
    public void getKeysFromPattern() {
        when(redisTemplate.keys("*key*")).thenReturn(new HashSet<>(Arrays.asList("key-1", "key-2")));

        Set<String> keys = redisClient.getKeysFromPattern("*key*");

        assertNotNull(keys);
        assertFalse(keys.isEmpty());
        assertTrue(keys.contains("key-1"));
    }

    @Test
    public void testFallbackForSetElement() {
        Deencapsulation.invoke(redisClient, "fallbackForSetElementInRedis", "key", "value");

        assertTrue(outContent.toString().contains("key"));
        assertTrue(outContent.toString().contains("value"));

        restoreStreams();
        initiateStreams();

        Deencapsulation.invoke(
                redisClient, "fallbackForDeleteElementFromRedis", "key");

        assertTrue(outContent.toString().contains("key"));
        assertTrue(outContent.toString().contains("Redis Delete Failed"));

        restoreStreams();
        initiateStreams();

        Deencapsulation.invoke(
                redisClient, "fallbackForPutInDBUsingHashOps", "key", "hashKey", "value");

        assertTrue(outContent.toString().contains("key"));
        assertTrue(outContent.toString().contains("Redis Put Using HashOps Failed"));

        restoreStreams();
        initiateStreams();

        Deencapsulation.invoke(
                redisClient, "fallbackForGetKeysFromPattern", "*key*");

        assertTrue(outContent.toString().contains("*key*"));
        assertTrue(outContent.toString().contains("Fallback for get keys from pattern executed"));
    }
}