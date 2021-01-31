package org.masood.mafia.domain

import info.mukel.telegrambot4s.models.User
import org.masood.mafia.domain.Player.PlayerStatus.PlayerStatus

object Player {
  //val KNOWN_CHARS = List("mafia", "citizen", "silencer", "lecter", "godfather", "sniper", "bishop", "doctor")
  def isMafia(character: String): Boolean = character.trim.toLowerCase match {
    case "mafia" | "godfather" | "lecter" | "silencer" => true
    case _ => false
  }

  object PlayerStatus extends Enumeration {
    type PlayerStatus = Value
    val Alive, Dead, Immortal = Value
  }

}

case class Player(user: User,
                  character: String,
                  status: PlayerStatus)
