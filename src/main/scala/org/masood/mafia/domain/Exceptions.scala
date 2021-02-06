package org.masood.mafia.domain

case class GameNotFoundException(val gameId: String) extends Exception

case class NotAuthorizedException(val gameId: String) extends Exception

case class TooManyArgumentsException(val charSum: Int, val people: Int) extends Exception
