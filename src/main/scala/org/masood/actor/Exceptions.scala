package org.masood.actor

case class ActorNotFoundException(val gameId: String) extends Exception
