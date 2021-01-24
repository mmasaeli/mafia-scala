package org.masood.messenger.telegram.bot

import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App {

  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.DEBUG

  def main(args: Array[String]): Unit = {
    runBot()
  }

  def runBot(): Unit = {
    // To run spawn the bot
    val bot = new MafiaBot(sys.env("TELEGRAM_TOKEN"))
    val eol = bot.run()
    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
    scala.io.StdIn.readLine()
    bot.shutdown()
    Await.result(eol, Duration.Inf)
  }
}

