package com.accenture.challenge.config;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName("localhost"); // replace with your Redis host
        factory.setPort(6379); // replace with your Redis port if different
        return factory;
    }

    @Bean
    public RedisTemplate<String, Order> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Order> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Order.class));
        return template;
    }

    public Jackson2JsonRedisSerializer<Object> redisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Register JavaTimeModule for LocalDateTime
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }
}
