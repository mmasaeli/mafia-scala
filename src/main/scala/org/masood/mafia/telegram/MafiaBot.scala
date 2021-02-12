package org.masood.mafia.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{DeleteMessage, EditMessageReplyMarkup, SendMessage}
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

  def iAmGodCommand(implicit msg: Message) {
    reply(s"Select or enter a game.",
      replyMarkup = Some(chooseGame("CLAIM_GAME")))
  }

  onCommand("help") { implicit msg =>
    helpCommand(getSession.status)
  }

  private def helpCommand(status: String)(implicit message: Message) = status match {
    case "JOINED" => reply(
      s"""/help: prints this message.
         |/new: starts a new game.
         |/join [game_id]: Join to a game.
         |/add player_name: Add a (fake) player to the game.
         |/disconnect [game_id]: Disconnect from the current game.
         |/iAmGod [game_id]: Become god if the game is godless.
         |""".stripMargin)
    case "GOD" => reply(
      s"""/help: prints this message.
         |/add player_name: Add a (fake) player to the game.
         |/disconnect [game_id]: Disconnect from the current game.
         |/cc [extra characters]: Set character counts. (start of randomization)
         |""".stripMargin)
    case _ => reply(
      s"""/help: prints this message.
         |/start: Start an interactive chat
         |/join [game_id]: Join a game.
         |/new: starts a new game.
         |/iAmGod [game_id]: Become god if the game is godless.
         |""".stripMargin)
  }

  private def newCommand(session: Session)(implicit msg: Message) = session.status match {
    case "EMPTY" =>
      val id = gameService.newGame.id
      sessionService.saveSession(session.copy(status = "GOD", gameId = id))
      reply(s"A new game has been initialized. ID: '$id'", replyMarkup = Some(god))
    case _ =>
      reply(s"You are in the middle of game ${session.gameId}",
        replyMarkup = Some(disconnectGame(session.gameId)))
  }

  onCommand("start") { implicit msg =>
    reply("Choose one of the options below:",
      replyMarkup = Some(start))
  }

  private def addCommand(args: Seq[String])(implicit msg: Message) = if (args.size == 1) {
    gameService.joinUser(getSession.gameId, Player(id = None, args.head))
  } else {
    reply("Give me some player name")
  }

  onCommand("session") { implicit msg =>
    reply(getSession.toString)
  }

  onCommand("reset") { implicit msg =>
    sessionService.saveSession(Session(userId = toPlayer.id.get, status = "EMPTY"))
  }

  onCommand("add") { implicit msg =>
    withArgs(args =>
      addCommand(args)
    )
  }

  private def disconnectCommand(session: Session)(implicit msg: Message) = session.gameId match {
    case null =>
      reply("You are not playing yet.")
    case _ =>
      reply(s"You are in the middle of game ${session.gameId}",
        replyMarkup = Some(disconnectGame(session.gameId)))
  }

  onCommand("new") { implicit msg =>
    newCommand(getSession)
  }

  onCommand("disconnect") { implicit msg =>
    disconnectCommand(getSession)
  }

  private def disconnectGame(gameId: String): InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      List(
        InlineKeyboardButton.callbackData(
          s"Disconnect from this Game!",
          prefixTag("DISCONNECT_GAME")(gameId))
        ,
        cancelButton("EMPTY")))
  }

  private def joinCommand(implicit msg: Message) =
    reply(s"Select or enter a game.",
      replyMarkup = Some(chooseGame("JOIN_GAME", List(GameStatus.New)))
    )

  private def chooseGame(tag: String, acceptableStatuses: List[GameStatus] = List()): ReplyMarkup =
    InlineKeyboardMarkup.singleColumn(
      gameService.listGames()
        .filter(game => acceptableStatuses.isEmpty || acceptableStatuses.contains(game.status))
        .map { game =>
          InlineKeyboardButton.callbackData(
            game.summary(),
            prefixTag(tag)(game.id)
          )
        }.toSeq ++ Seq(cancelButton("GOD"))
    )

  private def cancelButton(prevStatus: String) = InlineKeyboardButton.callbackData(
    "CANCEL", prefixTag("CANCEL")(prevStatus))

  onCommand("iAmGod") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        iAmGodCommand
      } else {
        claimGame(args.head, Player(msg.chat))
      })
  }

  private def ccCommand(args: Seq[String])(implicit msg: Message) = reply(
    s"""How many of these characters? Tap on a button to increase
       |Enter /randomize when all set
       |""".stripMargin,
    replyMarkup = Some(count(getSession.copy(status = "COUNTING",
      metadata = Map(("Mafia", 0), ("God father", 0), ("Doctor", 0), ("Armour", 0), ("Sniper", 0))
        ++ args.map { it => (it, 0) }.toMap), "")))

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
        joinCommand
      } else {
        joinGame(args.head, Player(msg.chat))
      }
    )
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
      }.toSeq ++ Seq(cancelButton("GOD"))
    )
  }

  onCallbackWithTag("COMMAND") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      val session = getSession(cbq.message.get)
      data match {
        case "JOIN" => joinCommand(msg)
        case "NEW" => newCommand(session)(msg)
        case "I_AM_GOD" => iAmGodCommand(msg)
        case "ADD" => addCommand(Seq())(msg)
        case "DISCONNECT" => disconnectCommand(session)(msg)
        case "CC" => ccCommand(Seq())(msg)
        case _ => helpCommand(session.status)(msg)
      }
    }
  }

  onCommand("all") { implicit msg =>
    withArgs(args =>
      if (msg.chat.id == 98257085) {
        reply(gameService.listGames().map { game =>
          if (args.nonEmpty && args.head == "detailed") game.toString else game.summary()
        }.mkString("\n______________________________________________________________________\n"))
      } else reply("/help to show all commands")
    )
  }


  onCommand("cc") { implicit msg =>
    withArgs(args => {
      ccCommand(args)
    })
  }

  private def start: InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      List(
        InlineKeyboardButton.callbackData(
          s"Join to a game",
          prefixTag("COMMAND")("JOIN")),
        InlineKeyboardButton.callbackData(
          s"Start a new game",
          prefixTag("COMMAND")("NEW")),
        InlineKeyboardButton.callbackData(
          s"Claim god 😇",
          prefixTag("COMMAND")("I_AM_GOD")),
        helpButton
      ))
  }

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
        sessionService.saveSession(getSession.copy(status = "JOINED", gameId = gameId))
        reply(res.get.toString, replyMarkup = Some(joined))
      case x if x.isFailure => x.failed.get match {
        case _: GameNotFoundException => reply(s"'$gameId' is not a valid game id")
        case _ =>
          logger.error("Error in joining user", x)
          reply("Sorry! Could not join the party for some unknown reason.")
      }
    }

  def getSession(implicit msg: Message): Session = sessionService.getSession

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
      case _ => reply(s"Failed to randomize characters.")
    }
  }

  private def joined: InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      List(
        InlineKeyboardButton.callbackData(
          s"Add a (fake) player",
          prefixTag("COMMAND")("ADD")),
        InlineKeyboardButton.callbackData(
          s"Disconnect",
          prefixTag("COMMAND")("DISCONNECT")),
        helpButton
      ))
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

  private def helpButton = InlineKeyboardButton.callbackData(
    "HELP", prefixTag("COMMAND")("HELP"))

  onCallbackWithTag("CANCEL") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      if (data != null)
        sessionService.saveSession(getSession(cbq.message.get).copy(status = data))
      request(
        DeleteMessage(
          ChatId(msg.source),
          msg.messageId
        ))
    }
  }

  private def claimGame(gameId: String, user: Player)(implicit msg: Message): Future[Message] =
    Try(gameService.claimGame(gameId, user)) match {
      case res if res.isSuccess =>
        sessionService.saveSession(sessionService.getSession(user.id.get)
          .copy(status = "GOD", gameId = gameId))
        reply(res.get.toString, replyMarkup = Some(god))
      case x if x.isFailure => x.failed.get match {
        case _: GameNotFoundException => reply(s"'$gameId' is not a valid game id")
        case _: NotAuthorizedException => reply(s"'The game already has a god.")
        case _ =>
          logger.error("Error in joining user", x)
          reply("Sorry! Could not do that for some unknown reason.")
      }
    }

  private def god: InlineKeyboardMarkup = {
    InlineKeyboardMarkup.singleColumn(
      List(
        InlineKeyboardButton.callbackData(
          s"Add a (fake) player",
          prefixTag("COMMAND")("ADD")),
        InlineKeyboardButton.callbackData(
          s"Disconnect",
          prefixTag("COMMAND")("DISCONNECT")),
        InlineKeyboardButton.callbackData(
          s"Count Characters and Randomize",
          prefixTag("COMMAND")("CC")),
        helpButton
      ))
  }

  onCallbackWithTag("DISCONNECT_GAME") { implicit cbq =>
    for {
      data <- cbq.data
      msg <- cbq.message
    } {
      gameService.disconnect(data)(Player(msg.chat))
      sessionService.saveSession(Session(userId = msg.chat.id, status = "EMPTY"))
      reply(s"Disconnected from game $data.")(msg)
    }
  }

}
