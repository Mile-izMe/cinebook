package com.cinebook.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}") private String redisHost;
    @Value("${spring.data.redis.port}") private int redisPort;
    @Value("${spring.data.redis.password:}") private String redisPassword;
    @Value("${spring.data.redis.username:}") private String redisUsername;
    @Value("${spring.data.redis.ssl.enabled:false}") private boolean redisSsl;

    // Package-visible for a configuration test without opening a network connection.
    Config clientConfig() {
        Config config = new Config();
        config.setThreads(2).setNettyThreads(2);
        var server = config.useSingleServer()
                .setAddress((redisSsl ? "rediss://" : "redis://") + redisHost + ":" + redisPort)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(4)
                .setSubscriptionConnectionMinimumIdleSize(1)
                .setSubscriptionConnectionPoolSize(2)
                .setPingConnectionInterval(60_000);
        if (!redisPassword.isBlank()) server.setPassword(redisPassword);
        if (!redisUsername.isBlank()) server.setUsername(redisUsername);
        return config;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        return Redisson.create(clientConfig());
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        // Ex enables keyevent notifications for expiry; required for realtime seat release.
        try (var connection = connectionFactory.getConnection()) {
            connection.setConfig("notify-keyspace-events", "Ex");
        }
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
