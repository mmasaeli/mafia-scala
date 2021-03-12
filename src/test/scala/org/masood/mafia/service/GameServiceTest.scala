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
import org.scalatest.matchers.must.Matchers.fullyMatch
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.junit.JUnitRunner
import org.springframework.data.redis.core.{HashOperations, RedisTemplate}

object GameServiceTest {
  implicit val god: Player = new Player(
    id = Some(0L),
    alias = "Zeus"
  )
  val users: List[Player] = List(
    new Player(id = Some(1L), alias = "Bart Simpson"),
    new Player(id = Some(3L), alias = "Lisa Simpson"),
    new Player(id = Some(2L), alias = "Homer Simpson"),
    new Player(id = Some(2L), alias = "Marge Simpson"),
    new Player(id = Some(2L), alias = "Maggie Simpson"),
    new Player(id = Some(2L), alias = "Ned Flanders"),
    new Player(id = Some(2L), alias = "Nelson"),
    new Player(id = Some(2L), alias = "Rod and Tod"),
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
  private val newGame = gameService.newGame

  override def beforeEach: Unit = {
    doNothing().when(hashOperations).put(ArgumentMatchers.eq("GAME"), any(), any())
  }

  override def afterEach() {
    reset(hashOperations)
  }

  test("Successful new game scenario") {
    newGame.id should fullyMatch.regex("[0-9]{6}".r)
    newGame shouldBe Game(newGame.id, List(god), Map(), New)
  }

  test("users should be able to join first") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq("666666"))
    ).thenReturn(newGame)
    val actualGame = gameService.joinUser("666666", users.head)
    actualGame.id should fullyMatch.regex("[0-9]{6}".r)
    actualGame shouldBe Game(newGame.id,
      List(god),
      Map((users.head.copy(alias = s"1. ${users.head.alias}"), "")),
      New
    )
  }

}