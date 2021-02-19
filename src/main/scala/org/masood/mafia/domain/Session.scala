package org.masood.mafia.domain

import org.masood.mafia.domain.PlayerStatus.PlayerStatus
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Session")
case class Session(userId: Long,
                   status: PlayerStatus,
                   gameId: String = null,
                   metadata: Map[String, Any] = Map()
                  ) extends JSerializable
