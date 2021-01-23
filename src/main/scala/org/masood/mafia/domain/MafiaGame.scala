package org.masood.mafia.domain

import com.bot4s.telegram.models.User

case class MafiaGame(id: String,
                     god: User,
                     players: List[Player],
                     chainNumber: Int)
