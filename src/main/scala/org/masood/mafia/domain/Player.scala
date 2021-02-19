package org.masood.mafia.domain

import info.mukel.telegrambot4s.models.Chat

case class Player(val id: Option[Long],
                  val alias: String) {
  override def toString: String = alias
}

object Player {
  def apply(chat: Chat): Player = new Player(
    id = Some(chat.id),
    alias = chat.firstName.getOrElse("!!!NO_NAME!!!") + chat.lastName.map { it => s" $it" }
      .getOrElse("") + s"(${chat.username.getOrElse("")})",
  )
}

object PlayerStatus extends Enumeration {
  type PlayerStatus = Value
  val New, Joined, Playing, Adding, Counting, God = Value

  def apply(str: String): Value = str.toUpperCase match {
    case "PLAYING" => Playing
    case "GOD" => God
    case "ADDING" => Adding
    case "COUNTING" => Counting
    case "JOINED" => Joined
    case _ => New
  }
}
