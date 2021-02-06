package org.masood.mafia.service

import com.typesafe.scalalogging.StrictLogging
import info.mukel.telegrambot4s.models.User
import org.apache.commons.lang.RandomStringUtils
import org.masood.mafia.domain._
import org.masood.mafia.repository.GameRepository
import org.springframework.stereotype.Service

@Service
class GameService(private val gameRepository: GameRepository) extends StrictLogging {

  def newGame(implicit god: User): Game = {
    val random: String = RandomStringUtils.randomNumeric(6)
    val game = Game(random, List(god), List(), Map(), GameStatus.New)
    gameRepository.save(game)
    logger.info(s"Created new game with ID '$random'.")
    game
  }

  def disconnect(gameId: String)(implicit user: User): Game =
    gameRepository.findById(gameId) match {
      case Some(game) =>
        gameRepository.save(
          game.copy(
            individuals = game.individuals.filter(_.id == user.id),
            gods = game.gods.filter(_.id == user.id))
        )
      case _ => throw GameNotFoundException(gameId)
    }

  def joinUser(gameId: String, user: User): Game =
    gameRepository.findById(gameId) match {
      case Some(game) => gameRepository.save(game.copy(individuals = game.individuals ++ List(user)))
      case _ => throw GameNotFoundException(gameId)
    }

  def randomize(gameId: String, characterCounts: Map[String, Int])(implicit user: User): Game =
    gameRepository.findById(gameId) match {
      case Some(game) =>
        if (game.gods.exists(_.id == user.id)) {
          if (characterCounts.values.sum > game.individuals.size) throw TooManyArgumentsException(characterCounts.values.sum, game.individuals.size)
          val users = game.individuals.sortBy(_ => Math.random)
          val charUsers: Map[User, String] = characterCounts.flatMap(pair =>
            users.zip(List.fill(pair._2)(pair._1)))
          gameRepository.save(game.copy(people = charUsers))
        } else {
          throw NotAuthorizedException(gameId)
        }
      case _ => throw GameNotFoundException(gameId)
    }

  def listGames() = gameRepository.findAll
}
