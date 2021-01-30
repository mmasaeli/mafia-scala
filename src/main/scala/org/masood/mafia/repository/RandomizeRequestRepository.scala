package org.masood.mafia.repository

import org.masood.mafia.domain.RandomizeRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RandomizeRequestRepository(val redisTemplate: RedisTemplate[String, RandomizeRequest]) {

  private val hashOperations = redisTemplate.opsForHash[String, RandomizeRequest]

  def save(randomizeRequest: RandomizeRequest) = {
    hashOperations.put("RAND_REQ", randomizeRequest.gameId, randomizeRequest)
  }
}
