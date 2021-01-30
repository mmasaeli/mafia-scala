package org.masood.mafia.config

import org.masood.mafia.domain.{Game, RandomizeRequest}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory

@Configuration
class Configurer {

  //  @Bean def redisConnectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379))
  //
  //  import org.springframework.context.annotation.Bean
  //  import org.springframework.data.redis.connection.RedisStandaloneConfiguration
  //  import org.springframework.data.redis.connection.jedis.JedisConnectionFactory

  @Bean
  def jedisConnectionFactory: JedisConnectionFactory = {
    val redisStandaloneConfiguration = new RedisStandaloneConfiguration("localhost", 6379)
    new JedisConnectionFactory(redisStandaloneConfiguration)
  }

  import org.springframework.context.annotation.Bean
  import org.springframework.data.redis.core.RedisTemplate

  @Bean
  def redisTemplate: RedisTemplate[String, Game] = {
    val redisTemplate = new RedisTemplate[String, Game]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }

  @Bean
  def redisTemplateRR: RedisTemplate[String, RandomizeRequest] = {
    val redisTemplate = new RedisTemplate[String, RandomizeRequest]
    redisTemplate.setConnectionFactory(jedisConnectionFactory)
    redisTemplate
  }
}
