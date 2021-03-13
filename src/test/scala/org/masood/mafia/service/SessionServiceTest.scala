package org.masood.mafia.service

import org.junit.runner.RunWith
import org.masood.mafia.domain.PlayerStatus.New
import org.masood.mafia.domain._
import org.masood.mafia.repository.SessionRepository
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.junit.JUnitRunner
import org.springframework.data.redis.core.{HashOperations, RedisTemplate}


@RunWith(classOf[JUnitRunner])
class SessionServiceTest extends AnyFunSuite with BeforeAndAfterEach {

  private val hashOperations = mock(classOf[HashOperations[String, String, Session]])
  private val template = mock(classOf[RedisTemplate[String, Session]])
  when(template.opsForHash[String, Session]).thenReturn(hashOperations)
  private val sessionRepo = new SessionRepository(template)
  private val sessionService = new SessionService(sessionRepo)

  override def afterEach() {
    reset(hashOperations)
  }

  test("should return new session for new user") {
    sessionService.getSession(0L) shouldBe Session(0L, New)
  }

}
