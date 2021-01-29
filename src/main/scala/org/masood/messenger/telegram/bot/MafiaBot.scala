package org.masood.messenger.telegram.bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.ChatId
import org.masood.actor.GameNotFoundException
import org.masood.mafia.service.GameService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import scala.concurrent.Future
import scala.util.{Failure, Try}

/** Generates random values.
 */
@Component
class MafiaBot(@Value("${TELEGRAM_TOKEN}") val token: String,
               val gameService: GameService) extends TelegramBot
  with Polling
  with Commands[Future] {

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
          case res if (res.isSuccess) => reply(res.get.toString).void
          case x if (x.isFailure) => x.failed.get match {
            case _: GameNotFoundException => reply(s"'${args.head}' is not a valid game id").void
            case ex =>
              logger.error("Error in joining user", x)
              reply("Sorry! Could not join the party for some unknown reason.").void
          }
        }
      }
    )
  }

  onCommand("randomize" | "rnd" | "rand") { implicit msg =>
    val f = request(SendMessage(ChatId(msg.chat.id), "How Many Mafia Players?"))
    f.onComplete {
      case Failure(e) => logger.error("Error in step #1 of randomization polling", e)
      case _ => reply("AAAAAA")
    }
    for {
      poll <- f
    } yield {
      reply(s"$poll sent")
    }
    f.void
  }

  // Int(n) extractor
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

}
