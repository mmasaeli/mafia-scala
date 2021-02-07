package org.masood.mafia.service

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang.RandomStringUtils
import org.masood.mafia.domain._
import org.masood.mafia.repository.GameRepository
import org.springframework.stereotype.Service

@Service
class GameService(private val gameRepository: GameRepository) extends StrictLogging {

  def newGame(implicit god: Player): Game = {
    val random: String = RandomStringUtils.randomNumeric(6)
    val game = Game(random, List(god), Map(), GameStatus.New)
    gameRepository.save(game)
    logger.info(s"Created new game with ID '$random'.")
    game
  }

  def disconnect(gameId: String)(implicit user: Player): Game =
    gameRepository.findById(gameId) match {
      case Some(game) =>
        val disconnected = game.copy(
          players = game.players.filterNot(_._1.id == user.id),
          gods = game.gods.filterNot(_.id == user.id))
        if (disconnected.players.isEmpty && disconnected.gods.isEmpty) {
          gameRepository.delete(gameId)
          disconnected
        } else gameRepository.save(disconnected)
      case _ => throw GameNotFoundException(gameId)
    }

  def joinUser(gameId: String, user: Player): Game =
    gameRepository.findById(gameId) match {
      case Some(game) => gameRepository.save(
        game.copy(players =
          game.players
            ++ Map((user.copy(alias = s"${game.players.size + 1}. ${user.alias}"), ""))))
      case _ => throw GameNotFoundException(gameId)
    }

  def claimGame(gameId: String, user: Player): Game =
    gameRepository.findById(gameId) match {
      case Some(game) => if (game.gods.isEmpty || game.gods.exists(_.id == user.id)) {
        gameRepository.save(game.copy(gods = List(user)))
      } else throw NotAuthorizedException(gameId)
      case _ => throw GameNotFoundException(gameId)
    }

  def randomize(gameId: String, characterCounts: Map[String, Int])(implicit user: Player): Game =
    gameRepository.findById(gameId) match {
      case Some(game) =>
        if (game.gods.exists(_.id == user.id)) {
          if (characterCounts.values.sum > game.players.size) throw TooManyArgumentsException(characterCounts.values.sum, game.players.size)
          val citizens = Map(("Citizen", game.players.size - characterCounts.values.sum))
          val expanded = (characterCounts ++ citizens)
            .flatMap { pair => List.fill(pair._2)(pair._1) }
          val charUsers: Map[Player, String] = game
            .players.keys.toList
            .sortBy(_ => Math.random)
            .zip(expanded)
            .toMap
          gameRepository.save(game.copy(players = charUsers, status = GameStatus.Randomized))
        } else {
          throw NotAuthorizedException(gameId)
        }
      case _ => throw GameNotFoundException(gameId)
    }

  def listGames(): Iterable[Game] = gameRepository.findAll
}
