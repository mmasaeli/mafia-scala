package org.masood.mafia.config

import org.masood.mafia.domain.{Game, RandomizeRequest, Session}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory


@Configuration
class RedisConfig {

  @Bean
  def jedisConnectionFactory: JedisConnectionFactory = {
    val redisStandaloneConfiguration = new RedisStandaloneConfiguration()
    new JedisConnectionFactory(redisStandaloneConfiguration)
  }

  import org.springframework.context.annotation.Bean
  import org.springframework.data.redis.core.RedisTemplate

  @Bean
  def gameTemplate: RedisTemplate[String, Game] = {
    val redisTemplate = new RedisTemplate[String, Game]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }

  @Bean
  def randomizeReqSession: RedisTemplate[String, RandomizeRequest] = {
    val redisTemplate = new RedisTemplate[String, RandomizeRequest]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }

  @Bean
  def sessionTemplate: RedisTemplate[String, Session] = {
    val redisTemplate = new RedisTemplate[String, Session]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }
}
