package com.kikimore.api.data.repository

import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.local.ProfileDao
import com.kikimore.api.data.local.UserDao
import com.kikimore.api.data.remote.UserRemoteDataSource
import com.kikimore.api.utils.Resource
import com.kikimore.api.utils.performGetOperation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    databaseQuery = { userLocalDataSource.allWithProfile() },
    networkCall = { remoteDataSource.getUsersSince(since) },
    saveCallResult = { userLocalDataSource.insert(it) }
  )

  fun getProfile(userName: String) = channelFlow<Resource<Profile>> {
    send(Resource.loading())
    var profile: Profile? = null
    launch {
      profileLocalDataSource.get(userName).collect {
        send(Resource.success(it))
        profile = it
      }
    }

    val networkCall = remoteDataSource.getUserProfile(userName)
    when(networkCall.status) {
      Resource.Status.SUCCESS -> {
        if (profile != null){
          networkCall.data?.copy(note = profile?.note)?.also {
            profileLocalDataSource.insert(it)
          }
        } else {
          networkCall.data?.also { profileLocalDataSource.insert(it) }
        }
      }
      Resource.Status.ERROR -> send(Resource.error(networkCall.message!!))
      else -> {}
    }
  }.flowOn(Dispatchers.IO)

  suspend fun getLastUser() = userLocalDataSource.getLast().firstOrNull()

  fun updateProfile(profile: Profile): Flow<Resource<Profile>> {
    return flow {
      profileLocalDataSource.update(profile)
      emit(Resource.success(profile))
    }
  }
}