package org.masood.mafia.telegram

import info.mukel.telegrambot4s.models.{Chat, ChatType, Message}
import org.junit.runner.RunWith
import org.masood.mafia.domain._
import org.masood.mafia.service.{GameService, SessionService}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

object MafiaBotTest {
  val eric = new Player(Some(1), "Eric Cartman(eric)")
}

@RunWith(classOf[JUnitRunner])
class MafiaBotTest extends AnyFunSpec with BeforeAndAfterEach {

  import MafiaBotTest._

  private val gameService = mock(classOf[GameService])
  private val sessionService = mock(classOf[SessionService])
  private val bot = new MafiaBot("TOKEN", 0L, "en", gameService, sessionService)

  override def afterEach(): Unit = {
    reset(gameService)
    reset(sessionService)
  }

  describe("Help") {
  }

  describe("New game") {
    it("should trigger on /new") {
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

  describe("Randomize") {
    it("should be able to perform char count with default characters") {

      when(sessionService.getSession(any(classOf[Chat]))).thenReturn(
        Session(
          userId = 1,
          status = PlayerStatus.God,
          gameId = "666666")
      )

      when(sessionService.saveSession(any(classOf[Session]))).thenAnswer {
        _.getArgument(0)
      }

      bot.receiveMessage(Message(
        messageId = 0, date = 0, chat = Chat(
          1, ChatType.Private, firstName = Some("Eric Cartman"), username = Some("eric")
        ), text = Some("/cc")
      ))
      verify(sessionService).saveSession(
        Session(
          userId = 1,
          status = PlayerStatus.Counting,
          gameId = "666666",
          metadata = Map(
            ("Mafia", 0),
            ("God father", 0),
            ("Doctor", 0),
            ("Armour", 0),
            ("Sniper", 0)
          )
        )
      )
    }
    it("should be able to perform char count with default and extra characters") {

      when(sessionService.getSession(any(classOf[Chat]))).thenReturn(
        Session(
          userId = 1,
          status = PlayerStatus.God,
          gameId = "666666")
      )

      when(sessionService.saveSession(any(classOf[Session]))).thenAnswer {
        _.getArgument(0)
      }

      bot.receiveMessage(Message(
        messageId = 0, date = 0, chat = Chat(
          1, ChatType.Private, firstName = Some("Eric Cartman"), username = Some("eric")
        ), text = Some("/cc cool char, no cool char")
      ))
      verify(sessionService).saveSession(
        Session(
          userId = 1,
          status = PlayerStatus.Counting,
          gameId = "666666",
          metadata = Map(
            ("Mafia", 0),
            ("God father", 0),
            ("Doctor", 0),
            ("Armour", 0),
            ("Sniper", 0),
            ("cool char", 0),
            ("no cool char", 0),
          )
        )
      )
    }
  }

}
