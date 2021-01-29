package org.masood.actor

case class ActorNotFoundException(val gameId: String) extends Exception

case class NotAuthorizedException(val gameId: String) extends Exception

case class TooManyArgumentsException(val gameId: String) extends Exception
