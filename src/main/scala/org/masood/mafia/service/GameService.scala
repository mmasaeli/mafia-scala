package org.masood.mafia.service

import org.masood.mafia.domain.GameStatus.GameStatus
import org.masood.mafia.domain.Player.PlayerStatus
import org.masood.mafia.domain.{Action, GameStatus, MafiaGame, Player}

class GameService {

  def state(game: MafiaGame): GameStatus = if(mafiaCount(game) >= cityCount(game)) GameStatus.Ended
  else game.chainNumber match {
    case 0 => GameStatus.New
    case 1 => GameStatus.MafiaRecognitionNight
    case x if(x % 2 == 0) => GameStatus.Day
    case _ => GameStatus.Night
  }

  def aliveCount(game: MafiaGame) = game.players.count(_.status != PlayerStatus.Dead)

  def mafiaCount(game: MafiaGame) = game.players
    .filter(p => Player.isMafia(p.character))
    .count(_.status != PlayerStatus.Dead)

  def cityCount(game: MafiaGame) = game.players
    .filter(p => !Player.isMafia(p.character))
    .count(_.status != PlayerStatus.Dead)

  def act(gameId: String, action: Action): Unit = ???

  def commit(): MafiaGame = ???
}
