package org.masood.mafia.service

//import com.bot4s.telegram.models.User
import com.typesafe.scalalogging.StrictLogging
import info.mukel.telegrambot4s.models.User
import org.apache.commons.lang.RandomStringUtils
import org.masood.mafia.domain._
import org.masood.mafia.repository.{GameRepository, RandomizeRequestRepository}
import org.springframework.stereotype.Service

@Service
class GameService(val gameRepository: GameRepository,
                  val randomizeRequestRepository: RandomizeRequestRepository) extends StrictLogging {

  def newGame(implicit god: User): Game = {
    val random: String = RandomStringUtils.randomNumeric(6)
    val game = Game(random, List(god), List(), Map(), GameStatus.New)
    gameRepository.save(game)
    logger.info(s"Created new game with ID '$random'.")
    game
  }

  def joinUser(gameId: String, user: User): Game =
    gameRepository.findById(gameId) match {
      case Some(game) => gameRepository.save(game.copy(individuals = game.individuals ++ List(user)))
      case _ => throw GameNotFoundException(gameId)
    }

  def randomizeRequest(gameId: String, user: User) =
    gameRepository.findById(gameId) match {
      case Some(game) => randomizeRequestRepository.save(RandomizeRequest(gameId, user.id, Map.empty[String, Int]))
      case _ => throw GameNotFoundException(gameId)
    }

  def randomizing(user: User) = randomizeRequestRepository.findById(user.id.toString)

  def randomizeRequest(randomizeRequest: RandomizeRequest, newChar: String, count: Int, user: User) =
    randomizeRequestRepository.findById(user.id.toString) match {
      case Some(rr) => randomizeRequestRepository.save(rr.copy(characterCounts = rr.characterCounts ++ Map(newChar -> count)))
      case _ => throw GameNotFoundException(randomizeRequest.gameId)
    }

  def randomize(randomizeRequest: RandomizeRequest, user: User): Game =
    gameRepository.findById(randomizeRequest.gameId) match {
      case Some(game) =>
        if (game.gods.exists(_.id == user.id)) {
          if (randomizeRequest.characterCounts.values.sum > game.individuals.size) throw TooManyArgumentsException(randomizeRequest.gameId)
          val users = game.individuals.sortBy(_ => Math.random)
          val charUsers: Map[User, String] = randomizeRequest.characterCounts.flatMap(pair =>
            users.zip(List.fill(pair._2)(pair._1)))
          gameRepository.save(game.copy(people = charUsers))
        } else {
          throw NotAuthorizedException(randomizeRequest.gameId)
        }
      case _ => throw GameNotFoundException(randomizeRequest.gameId)
    }

  def listGames() = gameRepository.findAll
}
