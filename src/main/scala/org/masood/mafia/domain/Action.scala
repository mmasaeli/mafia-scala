package org.masood.mafia.domain

//import org.masood.mafia.domain.MafiaAction.MafiaAction

case class Action(subj: List[Player], obj: List[Player], action: String)

//object MafiaAction extends Enumeration {
//  type MafiaAction = Value
//  val Assassin, Heal, Kill, Inquiry, Encircle = Value
//  val Custom = Value
//}
