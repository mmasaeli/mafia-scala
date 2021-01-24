package org.masood.messenger.telegram.bot

import akka.actor.{ActorSystem, PoisonPill, Props}
import com.bot4s.telegram.models.User
import org.masood.actor.GameActor

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App {
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

