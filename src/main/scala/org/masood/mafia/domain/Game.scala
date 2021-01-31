package org.masood.mafia.domain

import info.mukel.telegrambot4s.models.User
import org.masood.mafia.domain.GameStatus.GameStatus
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Game")
case class Game(id: String,
                gods: List[User],
                individuals: List[User],
                people: Map[User, String],
                state: GameStatus
               ) extends JSerializable
