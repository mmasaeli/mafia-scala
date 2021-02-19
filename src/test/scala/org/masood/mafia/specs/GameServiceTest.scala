package org.masood.mafia.specs

import org.junit.runner.RunWith
import org.masood.mafia.repository.GameRepository
import org.masood.mafia.service.GameService
import org.mockito.Mockito
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GameServiceTest extends AnyFunSuite {

  val gameRepo = Mockito.mock(classOf[GameRepository])
  val gameService = new GameService(gameRepo)

  test("An empty Set should have size 0") {
    Set.empty.size shouldBe 0
  }

  test("Invoking head on an empty Set should produce NoSuchElementException") {
    intercept[NoSuchElementException] {
      Set.empty.head
    }
  }
}