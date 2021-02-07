package org.masood.mafia.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, SendMessage}
import info.mukel.telegrambot4s.models._
import org.masood.mafia.domain.GameStatus.GameStatus
import org.masood.mafia.domain._
import org.masood.mafia.service.{GameService, SessionService}
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import scala.concurrent.Future
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

  implicit def toChat(implicit msg: Message): Chat = msg.chat

  implicit def toPlayer(implicit chat: Chat): Player = Player(chat)

  def getSession(implicit msg: Message): Session = sessionService.getSession

  onCommand("help") { implicit msg =>
    getSession.status match {
      case "ASSIGN" => reply("ASSIGNING")
      case "JOINED" => reply("JOINED")
      case "NEW" => reply("NEW")
      case "GOD" => reply("GOD")
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

  onCommand("add") { implicit msg =>
    withArgs(args =>
      if (args.size == 1) {
        gameService.joinUser(getSession.gameId, Player(id = None, args.head))
      } else {
        reply("Give me some player name")
      }
    )
  }

  onCommand("new") { implicit msg =>
    val session = getSession
    session.status match {
      case "EMPTY" =>
        val id = gameService.newGame.id
        sessionService.saveSession(session.copy(status = "GOD", gameId = id))
        reply(s"A new game has been initialized. ID: '$id'")
      case _ =>
        reply(s"You are in the middle of game ${session.gameId}",
          replyMarkup = Some(forgetGame(session.gameId)))
    }
  }

  private def chooseGame(tag: String, acceptableStatuses: List[GameStatus] = List()): ReplyMarkup = InlineKeyboardMarkup.singleColumn(
    gameService.listGames()
      .filter(game => acceptableStatuses.isEmpty || acceptableStatuses.contains(game.status))
      .map { game =>
        InlineKeyboardButton.callbackData(
          game.summary(),
          prefixTag(tag)(game.id)
        )
      }.toSeq
  )

  private def joinGame(gameId: String, user: Player)(implicit msg: Message): Future[Message] =
    Try(gameService.joinUser(gameId, user)) match {
      case res if res.isSuccess =>
        res.get.players.keys
          .filter(_.id.isDefined)
          .filterNot(_.id == user.id)
          .foreach { player =>
            request(SendMessage(
              chatId = player.id.get,
              s"${user.alias} joined the game!")
            )
          }
        res.get.gods.foreach { god =>
          request(SendMessage(
            chatId = god.id.get,
            s"""${user.alias} joined the game!
               |${res.get.toString}
               |""".stripMargin)
          )
        }
        reply(res.get.toString)
      case x if x.isFailure => x.failed.get match {
        case _: GameNotFoundException => reply(s"'$gameId' is not a valid game id")
        case ex =>
          logger.error("Error in joining user", x)
          reply("Sorry! Could not join the party for some unknown reason.")
      }
    }

  private def claimGame(gameId: String, user: Player)(implicit msg: Message): Future[Message] =
    Try(gameService.claimGame(gameId, user)) match {
      case res if res.isSuccess =>
        sessionService.saveSession(sessionService.getSession(user.id.get)
          .copy(status = "GOD", gameId = gameId))
        reply(res.get.toString)
      case x if x.isFailure => x.failed.get match {
        case _: GameNotFoundException => reply(s"'$gameId' is not a valid game id")
        case _: NotAuthorizedException => reply(s"'The game already has a god.")
        case ex =>
          logger.error("Error in joining user", x)
          reply("Sorry! Could not do that for some unknown reason.")
      }
    }

  onCommand("iAmGod") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply(s"Select or enter a game.",
          replyMarkup = Some(chooseGame("CLAIM_GAME")))
      } else {
        claimGame(args.head, Player(msg.chat))
      })
  }

  onCallbackWithTag("JOIN_GAME") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      joinGame(data, Player(msg.chat))(msg)
    }
  }

  onCallbackWithTag("CLAIM_GAME") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      claimGame(data, Player(msg.chat))(msg)
    }
  }

  onCommand("join") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply(s"Select or enter a game.",
          replyMarkup = Some(chooseGame("JOIN_GAME", List(GameStatus.New))))
      } else {
        joinGame(args.head, Player(msg.chat))
      }
    )
  }

  onCommand("all") { implicit msg =>
    withArgs(args =>
      if (msg.chat.id == 98257085) {
        reply(gameService.listGames().map { game =>
          if (args.nonEmpty && args.head == "detailed") game.toString else game.summary()
        }.mkString("\n"))
      } else reply("/help to show all commands")
    )
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
      val charCount = session.metadata.map { it => (it._1, it._2.toString.toInt) }
      val game = gameService.randomize(session.gameId, charCount)
      sessionService.saveSession(session.copy(status = "RANDOMIZED", metadata = Map()))
      game.players
        .filter(_._1.id.isDefined)
        .map { pair =>
          request(SendMessage(
            pair._1.id.get,
            s"""You are now a '${if (pair._2.isBlank) "Citizen" else pair._2}'.
               |The structure of the game: $charCount
               |""".stripMargin
          ))
        }
      reply(game.toString)
    } catch {
      case e: TooManyArgumentsException => reply(s"${e.people} people are present but ${e.charSum} characters are given.")
      case _: Throwable => reply(s"Failed to randomize characters.")
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

  onCallbackWithTag("HELP") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      reply(s"/$data")(msg)
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
      gameService.disconnect(data)(Player(msg.chat))
      sessionService.saveSession(Session(userId = msg.chat.id, status = "EMPTY"))
      reply(s"Forgot game $data.")(msg)
    }
  }

}
