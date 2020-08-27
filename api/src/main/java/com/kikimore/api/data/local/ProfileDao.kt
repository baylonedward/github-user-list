package com.kikimore.api.data.local

import androidx.room.Dao
import androidx.room.Query
import com.kikimore.api.data.entities.user.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */

@Dao
interface ProfileDao : BaseDao<Profile> {

  @Query("SELECT * FROM profiles WHERE id = :id")
  fun get(id: Int): Flow<Profile>

  @Query("SELECT * FROM profiles")
  fun all(): Flow<List<Profile>>
}