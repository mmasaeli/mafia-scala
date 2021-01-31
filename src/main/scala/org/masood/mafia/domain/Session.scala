package org.masood.mafia.domain

import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Session")
case class Session(userId: Int,
                   status: String,
                   metadata: Map[String, Any] = Map()
                  ) extends JSerializable
