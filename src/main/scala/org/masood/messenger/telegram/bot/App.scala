package org.masood.messenger.telegram.bot

import akka.actor.{ActorSystem, PoisonPill, Props}
import com.bot4s.telegram.models.User
import org.masood.actor.GameActor
import org.masood.messenger.telegram.bot.{MafiaBot => rb}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object App {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("mafia")
    val counter1 = system.actorOf(Props(new GameActor(new User(0, false,"Zeus"))), "Game")
    println(s"actor reference for $counter1")
    val actorSelection1 = system.actorSelection("counter")
    println(s"actor selection for $actorSelection1")
    counter1 ! PoisonPill
    Thread.sleep(100)
    system.terminate()
//    runBot()
  }

//  def runBot() = {
//    // To run spawn the bot
//    val bot = new rb("TOKEN")
//    val eol = bot.run()
//    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
//    scala.io.StdIn.readLine()
//    bot.shutdown() // initiate shutdown
//    // Wait for the bot end-of-life
//    Await.result(eol, Duration.Inf)
//  }
}

