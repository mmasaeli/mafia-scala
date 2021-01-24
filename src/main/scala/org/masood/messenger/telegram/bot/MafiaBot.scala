package org.masood.messenger.telegram.bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import org.masood.actor.ActorNotFoundException
import org.masood.mafia.service.GameService

import scala.concurrent.Future
import scala.util.Try

/** Generates random values.
 */
class MafiaBot(val token: String) extends TelegramBot
  with Polling
  with Commands[Future] {

  val gameService: GameService = new GameService

  // Or just the scalaj-http backend
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  onCommand("help" or "h") { implicit msg =>
    reply(
      s"""/help, /h: prints this message.
         |/new: starts a new game
         |/join, /j game_id: Join to a game
         |/report, /r [verbose, v]: prints game report. Use optional parameter verbose (or v) to get a detailed report for every night.
         |""".stripMargin).void
  }

  onCommand("new") { implicit msg =>
    val id = gameService.newGame(msg.from.get)
    reply(s"A new game has been initialized. ID: '$id'").void
  }

  onCommand("join" | "j") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply("Give me valid game id to join").void
      } else {
        Try(gameService.joinUser(args.head, msg.from.get)) match {
          case res if (res.isSuccess) => reply(res.get).void
          case x if (x.isFailure) => x.failed.get match {
            case _: ActorNotFoundException => reply(s"'${args.head}' is not a valid game id").void
            case ex =>
              logger.error("Error in joining user", x)
              reply("Sorry! Could not join the party for some unknown reason.").void
          }
        }
      }
    )
  }

  // Int(n) extractor
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

}
