# 1.0.2
                                                
                                                ## Updated Default Redis Configuration
                                                
                                                - Issue: [#1](https://github.com/pareshvpatil/redis-with-circuitbreaker/issues/1)
                                                
                                                - Redis Client considers default configuration for single instance
                                                    - default configuration considers single redis instance:
                                                        -   ```
                                                            spring.redis.sentinel.enabled=false
                                                            spring.redis.cluster.enabled=false
                                                            ```
                                                
                                                # 1.0.1
                                                
                                                ## Redis Client with circuitbreaker - Initial Commit
                                                
                                                - Enabled support for redis
                                                    - single instance configuration
                                                    - sentinel configuration
                                                    - cluster configuration
                                                - Hystrix command for individual redis operation