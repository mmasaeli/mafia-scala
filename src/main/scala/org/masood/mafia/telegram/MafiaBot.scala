package org.masood.mafia.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models._
import org.masood.mafia.domain.{GameNotFoundException, Session, TooManyArgumentsException}
import org.masood.mafia.service.{GameService, SessionService}
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import scala.util.Try

/** Generates random values.
 */
@Component
class MafiaBot(@Value("${TELEGRAM_TOKEN}") val token: String,
               private val gameService: GameService,
               private val sessionService: SessionService)
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
        sessionService.saveSession(session.copy(status = "NEW", gameId = id))
        reply(s"A new game has been initialized. ID: '$id'")
      case _ =>
        reply(s"You are in the middle of game ${session.gameId}",
          replyMarkup = Some(forgetGame(session.gameId)))
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

  onCommand("all") { implicit msg =>
    if (msg.from.get.id == 98257085) {
      reply(gameService.listGames().toString)
    } else reply("/help to show all commands")
  }


  onCommand("cc") { implicit msg =>
    withArgs(args => {
      reply(
        s"""How many of these characters? Tap on a button to increase
           |Press /randomize or /cancel at the end
           |""".stripMargin,
        replyMarkup = Some(count(getSession.copy(status = "COUNTING",
          metadata = Map(("Mafia", 0), ("God father", 0), ("Doctor", 0), ("Armour", 0), ("Sniper", 0))
            ++ args.map { it => (it, 0) }.toMap), "")))
    })
  }

  onCommand("randomize") { implicit msg =>
    val session = getSession
    try {
      val game = gameService.randomize(session.gameId, session.metadata.map { it => (it._1, it._2.toString.toInt) })
      session.copy(status = "RANDOMIZED", metadata = Map())
      reply(game.toString)
    } catch {
      case e: TooManyArgumentsException => reply(s"${e.people} people are present but ${e.charSum} characters are given.")
    }
  }

  private def count(session: Session, which: String): InlineKeyboardMarkup = {
    val newChars = session
      .metadata
      .map { ci => if (ci._1 == which) (ci._1, ci._2.toString.toInt + 1) else ci }
    sessionService.saveSession(session.copy(metadata = newChars))
    InlineKeyboardMarkup.singleColumn(
      newChars.map { pair =>
        InlineKeyboardButton.callbackData(
          pair.toString,
          prefixTag("COUNT_CHARS")(pair._1)
        )
      }.toSeq
    )
  }

  onCallbackWithTag("COUNT_CHARS") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      request(
        EditMessageReplyMarkup(
          Some(ChatId(msg.source)),
          Some(msg.messageId),
          replyMarkup = Some(count(sessionService.getSession(msg.chat.id), data)))
      )
    }
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
      sessionService.saveSession(Session(userId = msg.from.get.id, status = "EMPTY"))
      reply(s"Forgot game $data.")(msg)
    }
  }

}
