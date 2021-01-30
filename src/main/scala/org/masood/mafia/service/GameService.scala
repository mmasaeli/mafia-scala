package org.masood.mafia.service

import com.bot4s.telegram.models.User
import org.apache.commons.lang.RandomStringUtils
import org.masood.mafia.domain.GameStatus.GameStatus
import org.masood.mafia.domain.Player.PlayerStatus
import org.masood.mafia.domain._
import org.masood.mafia.repository.GameRepositoryImpl
import org.springframework.stereotype.Service
import slogging.StrictLogging

@Service
class GameService(gameRepository: GameRepositoryImpl) extends StrictLogging {

  def newGame(god: User): Game = {
    val random: String = RandomStringUtils.randomNumeric(6)
    val game = Game(random, List(god), List(), Map(), GameStatus.New)
    gameRepository.save(game)
    logger.info(s"Created new game with ID '$random'.")
    game
  }

  def joinUser(gameId: String, user: User): Game =
    gameRepository.sFindById(gameId) match {
      case Some(game) => gameRepository.save(game.copy(individuals = game.individuals ++ List(user)))
      case _ => throw GameNotFoundException(gameId)
    }

  def randomize(gameId: String, user: User, characterCounts: Map[String, Int]): Game =
    gameRepository.sFindById(gameId) match {
      case Some(game) =>
        if (game.gods.exists(_.id == user.id)) {
          if (characterCounts.values.sum > game.individuals.size) throw TooManyArgumentsException(gameId)
          val users = game.individuals.sortBy(_ => Math.random)
          val charUsers: Map[User, String] = characterCounts.flatMap(pair =>
            users.zip(List.fill(pair._2)(pair._1)))
          gameRepository.save(game.copy(people = charUsers))
        } else {
          throw NotAuthorizedException(gameId)
        }
      case _ => throw GameNotFoundException(gameId)
    }


  def state(game: MafiaGame): GameStatus = if (mafiaCount(game) >= cityCount(game)) GameStatus.Ended
  else game.chainNumber match {
    case 0 => GameStatus.New
    case 1 => GameStatus.MafiaRecognitionNight
    case x if (x % 2 == 0) => GameStatus.Day
    case _ => GameStatus.Night
  }

  def aliveCount(game: MafiaGame) = game.players.count(_.status != PlayerStatus.Dead)

  def mafiaCount(game: MafiaGame) = game.players
    .filter(p => Player.isMafia(p.character))
    .count(_.status != PlayerStatus.Dead)

  def cityCount(game: MafiaGame) = game.players
    .filter(p => !Player.isMafia(p.character))
    .count(_.status != PlayerStatus.Dead)
}
