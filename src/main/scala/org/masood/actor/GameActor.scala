package org.masood.actor

import akka.actor.Actor
import com.bot4s.telegram.models.User
import org.masood.mafia.domain.MafiaGame

object WorldActions {

  sealed trait WorldAction

  case class create(user: User) extends WorldAction

  case class Sunrise(user: User) extends WorldAction

  case class Sunset(user: User) extends WorldAction

  case class AddIndividual(user: User) extends WorldAction

  case class AssignCharacters(character: Map[String, User]) extends WorldAction

}

class GameActor(val god: User) extends Actor {

  import context._
  import org.masood.actor.WorldActions._

  var people: Seq[User] = Seq()
  var gameTale: List[MafiaGame] = List()
  var charactersToAssign: Map[String, Int] = _


  override def receive: Receive = {
    case AddIndividual(user) => {
      people = people ++ Seq(user)
      sender() ! s"User '${user.firstName}' added. Population: ${people.size}"
    }
    case AssignCharacters(characters) => {
      sender() ! s"Randomize done..."
    }
    case Sunset => become(mafiaRecognitionNight)
  }

  def mafiaRecognitionNight: Receive = {
    case Sunrise => become(day)
  }

  def day: Receive = {
    case Sunset => become(night)
  }

  def night: Receive = {
    case Sunrise => become(day)
  }
}
