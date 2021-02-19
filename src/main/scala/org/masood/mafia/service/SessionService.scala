package org.masood.mafia.service

import info.mukel.telegrambot4s.models.Chat
import org.masood.mafia.domain.{PlayerStatus, Session}
import org.masood.mafia.repository.SessionRepository
import org.springframework.stereotype.Service

@Service
class SessionService(private val sessionRepository: SessionRepository) {

  def getSession(implicit chat: Chat): Session = getSession(chat.id)

  def getSession(userId: Long): Session = sessionRepository.findById(userId) match {
    case Some(session) => session
    case _ => Session(userId = userId, status = PlayerStatus.New)
  }

  def saveSession(session: Session) = sessionRepository.save(session)

}
