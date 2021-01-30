package org.masood.mafia.domain

import java.io.{Serializable => JSerializable}

case class RandomizeRequest(gameId: String, characterCounts: Map[String, Int]) extends JSerializable
