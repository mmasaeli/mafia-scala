package org.masood.mafia.telegram

import info.mukel.telegrambot4s.models.{Chat, ChatType, Message}
import org.junit.runner.RunWith
import org.masood.mafia.domain._
import org.masood.mafia.service.{GameService, SessionService}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

object MafiaBotTest {
  val eric = new Player(Some(1), "Eric Cartman(eric)")
}

@RunWith(classOf[JUnitRunner])
class MafiaBotTest extends AnyFunSuite with BeforeAndAfterEach {

  import MafiaBotTest._

  private val gameService = mock(classOf[GameService])
  private val sessionService = mock(classOf[SessionService])
  private val bot = new MafiaBot("TOKEN", 0L, gameService, sessionService)

  override def afterEach(): Unit = {
    reset(gameService)
    reset(sessionService)
  }

  test("should trigger new game on /new") {
    when(sessionService.getSession(any(classOf[Chat]))).thenReturn(Session(1, PlayerStatus.New))

    when(gameService.newGame(any())).thenReturn(Game(
      "666666",
      List(eric),
      Map(),
      GameStatus.New)
    )

    bot.receiveMessage(Message(
      messageId = 0, date = 0, chat = Chat(
        1, ChatType.Private, firstName = Some("Eric Cartman"), username = Some("eric")
      ), text = Some("/new")
    ))
    verify(gameService).newGame(eric)
  }

}
