package org.masood.mafia.service

import org.junit.runner.RunWith
import org.masood.mafia.domain.GameStatus.{New, Randomized}
import org.masood.mafia.domain._
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
    new Player(id = Some(10L), alias = "Bart Simpson"),
    new Player(id = Some(32L), alias = "Lisa Simpson"),
    new Player(id = Some(43L), alias = "Homer Simpson"),
    new Player(id = Some(50L), alias = "Marge Simpson"),
    new Player(id = Some(60L), alias = "Maggie Simpson"),
    new Player(id = Some(70L), alias = "Ned Flanders"),
    new Player(id = Some(80L), alias = "Nelson"),
    new Player(id = Some(90L), alias = "Rod and Tod"),
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

  test("first users should be able to join with numbering") {
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

  test("all users should be able to join with numbering") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(newGame.copy(players = users.zipWithIndex.map(zipped =>
      (zipped._1.copy(alias = s"${zipped._2}. ${zipped._1.alias}"), "")
    ).toMap))
    val newPlayer = new Player(id = Some(104L), alias = "Milhouse")
    val actualGame = gameService.joinUser(newGame.id, newPlayer)
    actualGame shouldBe Game(newGame.id,
      List(god),
      (users.zipWithIndex.map(zipped => (
        zipped._1.copy(alias = s"${zipped._2}. ${zipped._1.alias}"), "")
      ) ++ List((newPlayer.copy(alias = s"${users.size + 1}. ${newPlayer.alias}"), ""))).toMap,
      New
    )
  }

  test("should be able to randomize") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(newGame.copy(players = users.zipWithIndex.map(zipped =>
      (zipped._1.copy(alias = s"${zipped._2}. ${zipped._1.alias}"), "")
    ).toMap))
    val actualGame = gameService.randomize(newGame.id, Map(
      ("A", 1),
      ("B", 2),
      ("C", 1),
    ))
    actualGame.status shouldBe Randomized
    actualGame.players.values.count(_ == "A") shouldBe 1
    actualGame.players.values.count(_ == "B") shouldBe 2
    actualGame.players.values.count(_ == "C") shouldBe 1
    actualGame.players.values.count(_ == "Citizen") shouldBe 4
  }

  test("should be able to report randomize too many chars error") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(newGame.copy(players = users.zipWithIndex.map(zipped =>
      (zipped._1.copy(alias = s"${zipped._2}. ${zipped._1.alias}"), "")
    ).toMap))
    intercept[TooManyArgumentsException](gameService.randomize(newGame.id, Map(
      ("A", 1),
      ("B", 2),
      ("C", 6),
    )))
  }

  test("should be able to disconnect") {
    val game = newGame.copy(players = users.zipWithIndex.map(zipped =>
      (zipped._1.copy(alias = s"${zipped._2}. ${zipped._1.alias}"), "")
    ).toMap)
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(game)
    val actualGame = gameService.disconnect(newGame.id)(users.head)
    actualGame shouldBe game.copy(players = game.players.filterKeys(_ != game.players.head._1))
  }

  test("should be able to claim games") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(newGame.copy(gods = List()))
    val actualGame = gameService.claimGame(newGame.id, users.head)
    actualGame shouldBe newGame.copy(gods = List(users.head))
  }

  test("should be able to report NotAuthorizedException on claim games") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), ArgumentMatchers.eq(newGame.id))
    ).thenReturn(newGame)
    intercept[NotAuthorizedException](gameService.claimGame(newGame.id, users.head))
  }

  test("should be able to report GameNotFoundException") {
    when(
      hashOperations.get(ArgumentMatchers.eq("GAME"), any())
    ).thenReturn(null)
    intercept[GameNotFoundException](gameService.disconnect("000000"))
    intercept[GameNotFoundException](gameService.joinUser("000000", users.head))
    intercept[GameNotFoundException](gameService.randomize("000000", Map()))
    intercept[GameNotFoundException](gameService.claimGame("000000", users.head))
  }

}
