package org.masood.mafia.service

import org.junit.runner.RunWith
import org.masood.mafia.domain.GameStatus.New
import org.masood.mafia.domain.{Game, Player}
import org.masood.mafia.repository.GameRepository
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.{fullyMatch, have}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.junit.JUnitRunner
import org.springframework.data.redis.core.{HashOperations, RedisTemplate}

object GameServiceTest {
  implicit val god: Player = new Player(
    id = Some(0L),
    alias = "Zeus"
  )
}

@RunWith(classOf[JUnitRunner])
class GameServiceTest extends AnyFunSuite with BeforeAndAfterEach {

  import GameServiceTest._

  private val hashOperations = mock(classOf[HashOperations[String, String, Game]])
  private val template = mock(classOf[RedisTemplate[String, Game]])
  when(template.opsForHash[String, Game]).thenReturn(hashOperations)
  private val gameRepo = new GameRepository(template)
  private val gameService = new GameService(gameRepo)

  override def beforeEach: Unit = {

  }

  override def afterEach() {
    reset(hashOperations)
  }

  test("Successful new game scenario") {
    doNothing().when(hashOperations).put(ArgumentMatchers.eq("GAME"), any(), any())
    val newGame = gameService.newGame
    newGame.id should fullyMatch.regex("[0-9]{6}".r)
    newGame shouldBe Game(newGame.id, List(god), Map(), New)
  }

}