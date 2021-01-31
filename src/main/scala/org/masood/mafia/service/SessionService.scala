package org.masood.mafia.service

import org.springframework.stereotype.Service

@Service
class SessionService(val sessionRepository: SessionRepository) {

  def getSession(user: User): Session = sessionRepository.findById(user.getId) match {
    case Some(session) => session
    case _ => Session(userId = user.getId, status = "EMPTY")
  }

}
