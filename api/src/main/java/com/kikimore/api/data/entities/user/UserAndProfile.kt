package com.kikimore.api.data.entities.user

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
data class UserAndProfile(
  @Embedded val user: User,
  @Relation(
    parentColumn = "id",
    entityColumn = "id"
  )
  val profile: Profile? = null
)