package org.masood.mafia.telegram

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.{Message, User}
import org.masood.mafia.domain.{GameNotFoundException, Session}
import org.masood.mafia.service.{GameService, SessionService}
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import scala.concurrent.Future
import scala.util.Try

/** Generates random values.
 */
@Component
class MafiaBot(@Value("${TELEGRAM_TOKEN}") val token: String,
               val gameService: GameService,
               val sessionService: SessionService)
  extends TelegramBot
    with Polling
    with Commands[Future] {

  implicit def toUser(implicit msg: Message): User = msg.from.get

  def getSession(implicit msg: Message): Session = sessionService.getSession

  // Or just the scalaj-http backend
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)
  onCommand("help" or "h") { implicit msg =>
    getSession.status match {
      case "ASSIGN" => reply("ASSIGNING").void
      case "JOINED" => reply("JOINED").void
      case _ =>
        reply(
          s"""/help, /h: prints this message.
             |/new: starts a new game
             |/join, /j game_id: Join to a game
             |/report, /r [verbose, v]: prints game report. Use optional parameter verbose (or v) to get a detailed report for every night.
             |""".stripMargin).void
    }
  }

  onCommand("new") { implicit msg =>
    val session = getSession
    val id = gameService.newGame(msg.from.get).id
    sessionService.saveSession(session.copy(status = "NEW", metadata = Map("gameId" -> id)))
    reply(s"A new game has been initialized. ID: '$id'").void
  }

  onCommand("join" | "j") {
    implicit msg =>
      withArgs(args =>
        if (args.size != 1) {
          reply("Give me valid game id to join").void
        } else {
          Try(gameService.joinUser(args.head, msg.from.get)) match {
            case res if (res.isSuccess) => reply(res.get.toString).void
            case x if (x.isFailure) => x.failed.get match {
              case _: GameNotFoundException => reply(s"'${
                args.head
              }' is not a valid game id").void
              case ex =>
                logger.error("Error in joining user", x)
                reply("Sorry! Could not join the party for some unknown reason.").void
            }
          }
        }
      )
  }

  onCommand("charCount" | "char" | "c") {
    implicit msg =>
      withArgs(args =>
        if (args.size != 1) {
          reply("Give me valid game id to initiate character combination count").void
        } else {
          Try(gameService.randomizeRequest(args.head, msg.from.get)) match {
            case res if (res.isSuccess) => reply(
              """Initiated now enter pairs of {'Character' 'Count'}(example: Mafia 3)
                |Enter /rand to randomize
                |""".stripMargin).void
          }
        }
      )
  }

  onCommand("rand" | "r") {
    implicit msg =>
      gameService.randomizing(msg.from.get) match {
        case Some(rr) => reply(gameService.randomize(rr, msg.from.get).toString).void
      }
  }

  onMessage {
    implicit msg =>
      gameService.randomizing(msg.from.get) match {
        case Some(rr) =>
          val splt = msg.text.get.strip().split("\\s+")
          gameService.randomizeRequest(rr, splt.head, splt(1).toInt, msg.from.get)
          reply("").void
      }
  }

  onCommand("all") {
    implicit msg =>
      if (msg.from.get.id == 98257085) {
        reply(gameService.listGames().toString).void
      } else reply("/help to show all commands").void
  }

  // Int(n) extractor
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

}
