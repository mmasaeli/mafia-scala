package org.masood.mafia.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models._
import org.masood.mafia.domain.Session
import org.masood.mafia.service.{GameService, SessionService}
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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

  def forgetGameMessage(command: String)(implicit session: Session, msg: Message) =
    replyMd(
      s"""You are in the middle of game ${session.metadata("gameId")}.
         |Enter /force
         |""".stripMargin)

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
  //
  //  onCommand("session") { implicit msg =>
  //    reply(getSession.toString)
  //  }
  //
  //  def newGame(session: Session)(implicit msg: Message) = {
  //    val id = gameService.newGame.id
  //    sessionService.saveSession(session.copy(status = "NEW", metadata = Map("gameId" -> id)))
  //    reply(s"A new game has been initialized. ID: '$id'")
  //  }
  //
  //  onCommand("new") { implicit msg =>
  //    val session = getSession
  //    session.status match {
  //      case "EMPTY" => newGame(session)
  //      case _ =>
  //        reply(s"You are in the middle of game ${session.metadata("gameId")}")
  //      //        request()
  //    }
  //  }
  //
  //
  private val TAG = "COUNTER_TAG"

  private def tag: String => String = prefixTag(TAG)

  private var requestCount = 0

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = Some(markupCounter(0)))
  }

  private def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    InlineKeyboardMarkup.singleButton( // set a layout for the Button
      InlineKeyboardButton.callbackData( // create the button into the layout
        s"Press me!!!\n$n - $requestCount", // text to show on the button (count of the times hitting the button and total request count)
        tag(n.toString))) // create a callback identifier
  }

  onCallbackWithTag(TAG) { implicit cbq => // listens on all callbacks that START with TAG
    // Notification only shown to the user who pressed the button.
    ackCallback(Some(cbq.from.firstName + " pressed the button!"))
    // Or just ackCallback() - this is needed by Telegram!

    for {
      data <- cbq.data //the data is the callback identifier without the TAG (the count in our case)
      Extractors.Int(n) = data // extract the optional String to an Int
      msg <- cbq.message
    } /* do */ {
      request(
        EditMessageReplyMarkup( // to update the existing button - (not creating a new button)
          Some(ChatId(msg.source)), // msg.chat.id
          Some(msg.messageId),
          replyMarkup = Some(markupCounter(n + 1))))
    }
  }
  //
  //  onCommand("join" | "j") { implicit msg =>
  //    withArgs(args =>
  //      if (args.size != 1) {
  //        reply("Give me valid game id to join")
  //      } else {
  //        Try(gameService.joinUser(args.head, msg.from.get)) match {
  //          case res if (res.isSuccess) => reply(res.get.toString)
  //          case x if (x.isFailure) => x.failed.get match {
  //            case _: GameNotFoundException => reply(s"'${
  //              args.head
  //            }' is not a valid game id")
  //            case ex =>
  //              logger.error("Error in joining user", x)
  //              reply("Sorry! Could not join the party for some unknown reason.")
  //          }
  //        }
  //      }
  //    )
  //  }
  //
  //  onCommand("charCount" | "char" | "c") { implicit msg =>
  //    withArgs(args =>
  //      if (args.size != 1) {
  //        reply("Give me valid game id to initiate character combination count")
  //      } else {
  //        Try(gameService.randomizeRequest(args.head, msg.from.get)) match {
  //          case res if (res.isSuccess) => reply(
  //            """Initiated now enter pairs of {'Character' 'Count'}(example: Mafia 3)
  //              |Enter /rand to randomize
  //              |""".stripMargin)
  //        }
  //      }
  //    )
  //  }
  //
  //  onMessage { implicit msg =>
  //    val session = getSession
  //    session.status match {
  //      //      case
  //      case _ => reply("Didn't catch that. enter /help to see the available actions")
  //    }
  //  }
  //
  //  onCommand("all") { implicit msg =>
  //    if (msg.from.get.id == 98257085) {
  //      reply(gameService.listGames().toString)
  //    } else reply("/help to show all commands")
  //  }

}
