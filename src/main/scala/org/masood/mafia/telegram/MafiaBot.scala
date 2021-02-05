package org.masood.mafia.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models._
import org.masood.mafia.domain.{GameNotFoundException, Session}
import org.masood.mafia.service.{GameService, SessionService}
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import scala.util.Try

/** Generates random values.
 */
@Component
class MafiaBot(@Value("${TELEGRAM_TOKEN}") val token: String,
               val gameService: GameService,
               val sessionService: SessionService)
  extends TelegramBot
    with Polling
    with Commands
    with Callbacks {
  implicit def toUser(implicit msg: Message): User = msg.from.get

  def getSession(implicit msg: Message): Session = sessionService.getSession

  onCommand("help") { implicit msg =>
    getSession.status match {
      case "ASSIGN" => reply("ASSIGNING")
      case "JOINED" => reply("JOINED")
      case "NEW" => reply("NEW")
      case _ => reply(
        s"""/help, /h: prints this message.
           |/new: starts a new game
           |/join, /j game_id: Join to a game
           |/report, /r [verbose, v]: prints game report. Use optional parameter verbose (or v) to get a detailed report for every night.
           |""".stripMargin)
    }
  }

  onCommand("session") { implicit msg =>
    reply(getSession.toString)
  }

  onCommand("new") { implicit msg =>
    val session = getSession
    session.status match {
      case "EMPTY" =>
        val id = gameService.newGame.id
        sessionService.saveSession(session.copy(status = "NEW", metadata = Map("gameId" -> id)))
        reply(s"A new game has been initialized. ID: '$id'")
      case _ =>
        reply(s"You are in the middle of game ${session.metadata("gameId")}",
          replyMarkup = Some(forgetGame(session.metadata("gameId").toString)))
    }
  }

  onCommand("join") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply("Give me valid game id to join")
      } else {
        Try(gameService.joinUser(args.head, msg.from.get)) match {
          case res if (res.isSuccess) => reply(res.get.toString)
          case x if (x.isFailure) => x.failed.get match {
            case _: GameNotFoundException => reply(s"'${
              args.head
            }' is not a valid game id")
            case ex =>
              logger.error("Error in joining user", x)
              reply("Sorry! Could not join the party for some unknown reason.")
          }
        }
      }
    )
  }

  private def forgetGame(gameId: String): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleButton(
      InlineKeyboardButton.callbackData(
        s"Forget this Game!\n",
        prefixTag("FORGET_GAME")(gameId)))
  }

  onCallbackWithTag("FORGET_GAME") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      gameService.disconnect(data)(msg.from.get)
      val session = getSession(msg)
      sessionService.saveSession(session.copy(
        status = "EMPTY",
        metadata = session.metadata.filter(it => it._1 == "gameId" && it._2 == data)))
      reply(s"Forgot game $data.")(msg)
    }
  }

  onCommand("cc") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply("Give me valid game id to initiate character combination count")
      } else {
        Try(gameService.randomizeRequest(args.head, msg.from.get)) match {
          case res if (res.isSuccess) => reply(
            """Initiated now enter pairs of {'Character' 'Count'}(example: Mafia 3)
              |Enter /rand to randomize
              |""".stripMargin)
        }
      }
    )
  }

  onCommand("all") { implicit msg =>
    if (msg.from.get.id == 98257085) {
      reply(gameService.listGames().toString)
    } else reply("/help to show all commands")
  }

}
