package org.masood.mafia.repository

import org.masood.mafia.domain.Session
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class SessionRepository(val redisTemplate: RedisTemplate[String, Session]) {

  private val hashOperations = redisTemplate.opsForHash[String, Session]

  def save(session: Session) = {
    hashOperations.put("SESSION", session.userId.toString, session)
  }

  def findById(id: Int) = Option(hashOperations.get("SESSION", id.toString))
}
