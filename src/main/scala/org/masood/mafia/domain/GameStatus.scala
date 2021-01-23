package org.masood.mafia.domain

object GameStatus extends Enumeration {
  type GameStatus = Value
  val New, MafiaRecognitionNight, Day, Night, Ended = Value
}
