package org.masood.mafia.config

import org.masood.mafia.domain.{Game, Session}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate


@Configuration
class RedisConfig(@Value("${REDIS_HOST:localhost}") val redisHost: String,
                  @Value("${REDIS_PORT:6379}") val redisPort: Int) {

  @Bean
  def gameTemplate: RedisTemplate[String, Game] = {
    val redisTemplate = new RedisTemplate[String, Game]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }

  import org.springframework.context.annotation.Bean
  import org.springframework.data.redis.core.RedisTemplate

  @Bean
  def jedisConnectionFactory: JedisConnectionFactory = {
    val redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort)
    new JedisConnectionFactory(redisStandaloneConfiguration)
  }

  @Bean
  def sessionTemplate: RedisTemplate[String, Session] = {
    val redisTemplate = new RedisTemplate[String, Session]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }
}
