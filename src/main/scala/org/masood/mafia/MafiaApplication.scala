package org.masood.mafia

import org.masood.mafia.telegram.MafiaBot
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext

object MafiaApplication extends App {
  SpringApplication.run(classOf[MafiaApplication])
  val context = new AnnotationConfigApplicationContext(classOf[MafiaApplication])
  val bot = context.getBean("mafiaBot", classOf[MafiaBot])
  bot.run()
}

@SpringBootApplication
class MafiaApplication
