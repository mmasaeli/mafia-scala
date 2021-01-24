package org.masood.messenger.telegram.bot

import akka.actor.{ActorSystem, Props}
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import org.apache.commons.lang.RandomStringUtils
import org.masood.actor.GameActor
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{Await, Future}
import scala.util.Try

/** Generates random values.
 */
class MafiaBot(val token: String) extends TelegramBot
  with Polling
  with Commands[Future] {

  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  private val ctx: ActorSystem = ActorSystem("mafia")

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
    val random: String = RandomStringUtils.randomNumeric(6)
    ctx.actorOf(Props(new GameActor(msg.from.get)), s"actor-$random")
    reply(s"A new game has been initialized. ID: '$random'").void
  }

  onCommand("join" | "j") { implicit msg =>
    withArgs(args =>
      if (args.size != 1) {
        reply("Give me valid game id to join").void
      } else {
        val actorSelection = ctx.actorSelection(s"/user/actor-${args.head}/")
        val resolved = actorSelection.resolveOne(Duration(700, MILLISECONDS))
        val ready = Await.ready(resolved, Duration(700, MILLISECONDS))

        if (ready.value.isDefined && ready.value.get.isSuccess) {
          reply(s"${ready.value.get.get}").void
        } else {
          reply(s"'${args.head}' is not a valid game id").void
        }
      }
    )
  }

  // Int(n) extractor
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

}
