package org.masood.mafia.service

import com.bot4s.telegram.models.User
import org.masood.mafia.domain.Session
import org.masood.mafia.repository.SessionRepository
import org.springframework.stereotype.Service

@Service
class SessionService(val sessionRepository: SessionRepository) {

  def getSession(implicit user: User): Session = sessionRepository.findById(user.id) match {
    case Some(session) => session
    case _ => Session(userId = user.id, status = "EMPTY")
  }

  def saveSession(session: Session) = sessionRepository.save(session)

}
