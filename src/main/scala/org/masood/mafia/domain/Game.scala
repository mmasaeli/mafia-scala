package org.masood.mafia.domain

import org.masood.mafia.domain.GameStatus.GameStatus
import org.springframework.data.redis.core.RedisHash

import java.io.{Serializable => JSerializable}

@RedisHash("Game")
case class Game(id: String,
                gods: List[Player],
                players: Map[Player, String],
                state: GameStatus
               ) extends JSerializable {
  def summary(): String = s"$id: $state, God(s):[${
    gods.map { god =>
      s"{${god.alias}}"
    }.mkString(", ")
  }]. ${players.size} players."

  override def toString: String = s"$id: $state, God(s):[${
    gods.map { god =>
      s"{${god.alias}}"
    }.mkString(", ")
  }]. Players: [${
    players.map { it =>
      s"{${it._1.alias}${if (it._2 == "") "" else s" -> '${it._2}'"}}"
    }.mkString(", ")
  }]"
}
