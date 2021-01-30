package org.masood.mafia.domain

import com.bot4s.telegram.models.User
import org.masood.mafia.domain.GameStatus.{GameStatus, Invalid}
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Game")
case class Game(
                 @Id
                 val id: String,
                 val gods: List[User],
                 val individuals: List[User],
                 val people: Map[User, String],
                 val state: GameStatus
               ) extends JSerializable

object Game {
  def InvalidGame = Game("", List(), List(), Map(), Invalid)
}
