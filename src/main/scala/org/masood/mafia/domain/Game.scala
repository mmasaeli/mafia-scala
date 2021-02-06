package org.masood.mafia.domain

import info.mukel.telegrambot4s.models.User
import org.masood.mafia.domain.GameStatus.GameStatus
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Game")
case class Game(id: String,
                gods: List[User],
                people: Map[User, String],
                state: GameStatus
               ) extends JSerializable {
  def summary(): String = s"$id: $state, God(s):{${
    gods.map {
      _.firstName
    }.mkString(", ")
  }}. ${people.size} players."

  override def toString: String = s"$id: $state, God(s):{${
    gods.map {
      _.firstName
    }.mkString(", ")
  }}. Players: {${
    people.map { it =>
      s"'${it._1.firstName}(${it._1.username.getOrElse("")})' ${if (it._2 == "") "" else " -> '${it._2}'"}"
    }.mkString(", ")
  }}"
}
