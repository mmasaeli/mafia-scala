package org.masood.mafia.service

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.bot4s.telegram.models.User
import org.apache.commons.lang.RandomStringUtils
import org.masood.actor.{ActorNotFoundException, GameActor}
import org.masood.actor.WorldActions.AddIndividual
import org.masood.mafia.domain.GameStatus.GameStatus
import org.masood.mafia.domain.Player.PlayerStatus
import org.masood.mafia.domain.{GameStatus, MafiaGame, Player}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MILLISECONDS}

class GameService {

  private val ctx: ActorSystem = ActorSystem("mafia")
  private val duration = Duration(700, MILLISECONDS)
  private implicit val timeout = Timeout(duration)

  def newGame(god: User): String = {
    val random: String = RandomStringUtils.randomNumeric(6)
    ctx.actorOf(Props(new GameActor(god)), s"actor-$random")
    random
  }

  def joinUser(gameId: String, user: User): String = {
    val actor = findActor(gameId)
    Await.result(actor ? AddIndividual(user), duration).toString
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

  def findActor(id: String): ActorRef = {
    val actorSelection = ctx.actorSelection(s"/user/actor-$id/")
    val resolved = actorSelection.resolveOne(duration)
    val ready = Await.ready(resolved, duration)

    if (ready.value.isDefined && ready.value.get.isSuccess) {
      ready.value.get.get
    } else {
      throw ActorNotFoundException(id)
    }
  }
}
