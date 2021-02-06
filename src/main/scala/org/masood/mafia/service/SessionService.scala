package org.masood.mafia.service

import info.mukel.telegrambot4s.models.User
import org.masood.mafia.domain.Session
import org.masood.mafia.repository.SessionRepository
import org.springframework.stereotype.Service

@Service
class SessionService(private val sessionRepository: SessionRepository) {

  def getSession(implicit user: User): Session = getSession(user.id)

  def getSession(userId: Int): Session = sessionRepository.findById(userId) match {
    case Some(session) => session
    case _ => Session(userId = userId, status = "EMPTY")
  }

  def saveSession(session: Session) = sessionRepository.save(session)

}
