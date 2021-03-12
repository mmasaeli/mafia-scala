package org.masood.mafia.repository

import org.masood.mafia.domain.Game
import org.springframework.data.redis.core.{HashOperations, RedisTemplate}
import org.springframework.stereotype.Repository

import scala.collection.JavaConverters._

@Repository
class GameRepository(val redisTemplate: RedisTemplate[String, Game]) {

  private val hashOperations = redisTemplate.opsForHash[String, Game]

  def save(game: Game): Game = {
    hashOperations.put("GAME", game.id, game)
    game
  }

  def findAll = hashOperations.entries("GAME").values().asScala

  def findById(id: String): Option[Game] = Option(hashOperations.get("GAME", id))

  def update(game: Game): Game = {
    save(game)
  }

  def delete(id: String): Unit = {
    hashOperations.delete("GAME", id)
  }
}
