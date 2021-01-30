package org.masood.mafia.repository

import org.masood.mafia.domain.Game
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

import java.lang
import java.util.Optional
import scala.collection.JavaConverters._


@Repository
class GameRepositoryImpl(val redisTemplate: RedisTemplate[String, Game]) extends GameRepository {

  private val hashOperations = redisTemplate.opsForHash[String, Game]

  override def save[S <: Game](game: S): S = {
    hashOperations.put("GAME", game.id, game)
    game
  }

  override def findAll = hashOperations.entries("GAME").values()

  def sFindById(id: String): Option[Game] = Some(hashOperations.get("GAME", id))

  def update(game: Game): Game = {
    save(game)
  }

  def delete(id: String): Unit = {
    hashOperations.delete("GAME", id)
  }

  override def saveAll[S <: Game](entities: lang.Iterable[S]): lang.Iterable[S] = ???

  override def findById(id: String): Optional[Game] = ???

  override def existsById(id: String): Boolean = ???

  override def findAllById(ids: lang.Iterable[String]): lang.Iterable[Game] = ???

  override def count(): Long = ???

  override def deleteById(id: String): Unit = ???

  override def delete(game: Game): Unit = delete(game.id)

  override def deleteAll(entities: lang.Iterable[_ <: Game]): Unit = ???

  override def deleteAll(): Unit = ???
}
