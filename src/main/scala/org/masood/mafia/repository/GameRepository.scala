package org.masood.mafia.repository

import org.masood.mafia.domain.Game
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
trait GameRepository extends CrudRepository[Game, String] {}
