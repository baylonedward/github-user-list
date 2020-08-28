package com.kikimore.api.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.entities.user.UserAndProfile
import kotlinx.coroutines.flow.Flow

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@Dao
interface UserDao : BaseDao<User> {

  @Query("SELECT * FROM users")
  fun all(): Flow<List<User>>

  @Query("SELECT * FROM users WHERE id = :id")
  fun get(id: Int): Flow<User>

  @Transaction
  @Query("SELECT * FROM users")
  fun allWithProfile(): Flow<List<UserAndProfile>>

  @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
  fun getLast(): Flow<User>
}