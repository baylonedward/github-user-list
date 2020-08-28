package com.kikimore.api.data.repository

import com.kikimore.api.data.local.ProfileDao
import com.kikimore.api.data.local.UserDao
import com.kikimore.api.data.remote.UserRemoteDataSource
import com.kikimore.api.utils.performGetOperation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class UserRepository(
  private val remoteDataSource: UserRemoteDataSource,
  private val userLocalDataSource: UserDao,
  private val profileLocalDataSource: ProfileDao

) {

  fun getUsers(since: Int = 0) = performGetOperation(
    databaseQuery = { userLocalDataSource.all() },
    networkCall = { remoteDataSource.getUsersSince(since) },
    saveCallResult = { userLocalDataSource.insert(it) }
  )

  fun getProfile(userName: String) = performGetOperation(
    databaseQuery = { profileLocalDataSource.get(userName) },
    networkCall = { remoteDataSource.getUserProfile(userName) },
    saveCallResult = { profileLocalDataSource.insert(it) }
  )

  suspend fun hasProfileNote(id: Int): Boolean {
    val hasNotes = CompletableDeferred<Boolean>()
    hasNotes.complete(profileLocalDataSource.get(id).firstOrNull()?.note != null)
    return hasNotes.await()
  }

  suspend fun getLastUser() = userLocalDataSource.getLast().firstOrNull()
}