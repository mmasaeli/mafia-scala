package org.masood.messenger.telegram.bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.util.Try
import scala.concurrent.Future

/** Generates random values.
 */
class MafiaBot(val token: String) extends TelegramBot
  with Polling
  with Commands[Future] {

  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  // Or just the scalaj-http backend
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)

  onCommand("help" or "h") { implicit msg =>
    reply(
      s"""/help, /h: prints this message.
         |/new: starts a new game
         |/report, /r [verbose, v]: prints game report. Use optional parameter verbose (or v) to get a detailed report for every night.
         |""".stripMargin).void
  }

  onCommand("new") { implicit msg =>
    withArgs( args =>
      reply(s"$msg  0000000 $args").void
    )
  }

  // Int(n) extractor
  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }
}
