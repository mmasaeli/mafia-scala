package org.masood.mafia.repository

import org.masood.mafia.domain.{Game, RandomizeRequest}
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository


@Repository
class GameRepositoryImpl(val redisTemplate: RedisTemplate[String, Game]
                         , val redisTemplateRR: RedisTemplate[String, RandomizeRequest]) {

  private val hashOperations = redisTemplate.opsForHash[String, Game]
  private val hashOperationsRR = redisTemplateRR.opsForHash[String, RandomizeRequest]

  def save(game: Game): Game = {
    hashOperations.put("GAME", game.id, game)
    game
  }

  def saveRandomizeReq(randomizeRequest: RandomizeRequest) = {
    hashOperationsRR.put("RAND_REQ", randomizeRequest.gameId, randomizeRequest)
  }

  def findAll = hashOperations.entries("GAME").values()

  def sFindById(id: String): Option[Game] = Option(hashOperations.get("GAME", id))

  def update(game: Game): Game = {
    save(game)
  }

  def delete(id: String): Unit = {
    hashOperations.delete("GAME", id)
  }
}
