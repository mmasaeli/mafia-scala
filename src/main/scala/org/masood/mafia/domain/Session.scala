package org.masood.mafia.domain

import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Session")
case class Session(userId: Int,
                   //                   chatId: Int,
                   status: String,
                   metadata: String = ""
                  ) extends JSerializable
