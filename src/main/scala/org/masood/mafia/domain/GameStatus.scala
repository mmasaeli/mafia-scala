package org.masood.mafia.domain

object GameStatus extends Enumeration {
  type GameStatus = Value
  val New, Randomized, MafiaRecognitionNight, Day, Night, Ended, Invalid = Value
}
