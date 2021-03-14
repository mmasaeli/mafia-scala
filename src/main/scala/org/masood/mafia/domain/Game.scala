package org.masood.mafia.domain

import org.masood.mafia.domain.GameStatus.GameStatus
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Game")
case class Game(id: String,
                gods: List[Player],
                players: Map[Player, String],
                status: GameStatus
               ) extends JSerializable {
  def summary(): String = s"Game: ID('$id'): $status, God(s):[${
    gods.map { god =>
      s"{${god.alias}}"
    }.mkString(", ")
  }]. ${players.size} players."

  override def toString: String = s"Game: ID('$id'): $status, God(s):[${
    gods.map { god =>
      s"\t- ${god.alias}"
    }.mkString("\n")
  }]. Players: [\n${
    players.map { it =>
      s"\t- ${it._1.alias}${if (it._2 == "") "" else s" -- '${it._2}'"}"
    }.mkString("\n")
  }]"
}
