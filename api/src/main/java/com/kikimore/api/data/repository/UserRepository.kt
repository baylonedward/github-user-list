package com.kikimore.api.data.repository

import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.local.ProfileDao
import com.kikimore.api.data.local.UserDao
import com.kikimore.api.data.remote.UserRemoteDataSource
import com.kikimore.api.utils.Resource
import com.kikimore.api.utils.performGetOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class UserRepository private constructor(
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
    // db call
    launch {
      profileLocalDataSource.get(userName).collect {
        send(Resource.success(it))
      }
    }

    // network call
    remoteDataSource.getUserProfile(userName).also { networkCall ->
      val profile = profileLocalDataSource.get(userName).firstOrNull()
      when (networkCall.status) {
        Resource.Status.SUCCESS -> {
          if (profile != null) {
            networkCall.data?.copy(note = profile.note)?.also {
              profileLocalDataSource.insert(it)
            }
          } else {
            networkCall.data?.also { profileLocalDataSource.insert(it) }
          }
        }
        Resource.Status.ERROR -> {
          send(Resource.error(networkCall.message!!))
          profile?.also { Resource.success(it) }
        }
        else -> {
        }
      }
    }
  }.flowOn(Dispatchers.IO)

  fun getLastUser() = flow {
    emit(userLocalDataSource.getLast().firstOrNull())
  }

  fun updateProfile(profile: Profile): Flow<Resource<Profile>> {
    val message = "Profile Updated!"
    return flow {
      profileLocalDataSource.update(profile).run {
        emit(Resource.success(profile, message))
      }
    }
  }

  fun hasUser() = channelFlow {
    userLocalDataSource.all().collect {
      send(it.count() > 0)
    }
    send(false)
  }

  companion object {
    @Volatile
    private var instance: UserRepository? = null

    fun getInstance(
      remoteDataSource: UserRemoteDataSource,
      userLocalDataSource: UserDao,
      profileLocalDataSource: ProfileDao
    ) = instance ?: synchronized(this) {
      println("UserRepository is null, creating new instance.")
      UserRepository(remoteDataSource, userLocalDataSource, profileLocalDataSource).also {
        instance = it
      }
    }
  }
}