package org.masood.mafia.domain

import info.mukel.telegrambot4s.models.Chat

case class Player(val id: Long,
                  val alias: String) {
  override def toString: String = alias
}

object Player {
  def apply(chat: Chat): Player = new Player(
    id = chat.id,
    alias = chat.firstName.getOrElse("!!!NO_NAME!!!") + chat.lastName.map { it => s" $it" }
      .getOrElse("") + s"(${chat.username.getOrElse("")})",
  )
}
