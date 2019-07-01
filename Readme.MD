# Redis Client with Circuit-Breaker

Spring boot redis client with redis calls wrapped with hystrix to monitor redis performance, with handled fallback responses.

This artifact enables communication with redis connection with sentinel, cluster and single instance configuration.

Ready to use methods like get, get from hash operations, put, put with expiry, put with hash operations and delete with their respective hystrix commands and their fallbacks available.

## Pre-requisites
- spring boot application with version **2.0.3** or greater

## How To Configure

- To use this artifact in your spring boot application, add the following dependency snippet in your **pom.xml**.
```
<dependency>
    <groupId>io.github.pareshvpatil</groupId>
    <artifactId>redis-with-circuitbreaker</artifactId>
    <version>{redis-client-version}</version>
</dependency>
```
- Find out the latest version of the artifact in [Central Repository](https://mvnrepository.com/artifact/io.github.pareshvpatil/redis-with-circuitbreaker)
- To use this artifact, standard spring boot redis configuration properties are used. Please find the sample here.
    - These configurations are for redis-single-instance
    ```
    spring.redis.host=localhost
    spring.redis.port=6379
    spring.redis.timeout=500
    spring.redis.pool.max-active=100
    spring.redis.pool.max-wait=1000
    spring.redis.pool.max-idle=100
    spring.redis.pool.min-idle=5
    ```
    - These configurations are for redis-cluster
    ```
    spring.redis.cluster.nodes=localhost:30001,localhost:30002,localhost:30003
    spring.redis.cluster.max-redirects=2
    spring.redis.jedis.pool.max-active=8
    spring.redis.jedis.pool.max-idle=8
    spring.redis.jedis.pool.max-wait=1ms
    spring.redis.jedis.pool.min-idle=0
    ```
    - These configurations are for redis-sentinel
    ```$xslt
    spring.redis.sentinel.master=my-master
    spring.redis.sentinel.nodes=localhost:6380,localhost:6381,localhost:6382
    spring.redis.jedis.pool.max-active=8
    spring.redis.jedis.pool.max-idle=8
    spring.redis.jedis.pool.max-wait=1ms
    spring.redis.jedis.pool.min-idle=0
    ```
- Along with redis configurations, for controlling the redis calls timeouts, and thread pools to be used to redis calls, following hystrix command properties could be used.<br>The following properties can be added to your **bootstrap.yml** file for easy circuit-breaker related configurations.<br>Note that the *hystrix.default.circuitbreaker.errorThresholdPercentage* property allows for a missed entry for a specific hystrix command declared inside your application. 
- For additional information on configurations related to circuit-breaker, visit [spring-boot-with-hystrix](https://spring.io/guides/gs/circuit-breaker/)
```$xslt
hystrix:
  metrics:
    enabled: true
    polling-interval-ms: 1000
  command:
    default:
      circuitBreaker:
        errorThresholdPercentage: 50
    redisClientGet:
      circuitBreaker:
        enabled: true
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
    redisClientSet:
      circuitBreaker:
        enabled: true
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
    redisClientDelete:
      circuitBreaker:
        enabled: true
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
```
## How To Use
- To Use the redis client in your code, simply inject it in your class by `@Autowired` annotation. Here's an example:
```$xslt
@Component
public class SessionServiceImpl implements SessionService {
    
    // ... other fields
    private RedisClient redisClient;

    public SessionServiceImpl(@Autowired RedisClient redisClient) {
        this.redisClient = redisClient;
    }
    
    // ... other methods
}
```

- A working sample is available on github [here](https://github.com)
