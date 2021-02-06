package org.masood.mafia.domain

import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Session")
case class Session(userId: Long,
                   status: String,
                   gameId: String = null,
                   metadata: Map[String, Any] = Map()
                  ) extends JSerializable
