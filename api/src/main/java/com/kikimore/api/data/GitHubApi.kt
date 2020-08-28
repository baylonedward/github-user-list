package com.kikimore.api.data

import android.app.Application
import android.content.Context
import com.kikimore.api.data.local.GitHubDatabase
import com.kikimore.api.data.remote.UserRemoteDataSource
import com.kikimore.api.data.remote.UserService
import com.kikimore.api.data.repository.UserRepository
import com.kikimore.api.utils.loggingInterceptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class GitHubApi(context: Context) {
  private val retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(GsonConverterFactory.create())
    .client(OkHttpClient.Builder().loggingInterceptor().build())
    .build()
  private val userService = retrofit.create(UserService::class.java)
  private val userRemoteDataSource = UserRemoteDataSource(userService)
  private val db = GitHubDatabase.getDatabase(context)

  fun userRepository(): UserRepository {
    return UserRepository(
      remoteDataSource = userRemoteDataSource,
      userLocalDataSource = db.userDao(),
      profileLocalDataSource = db.profileDao()
    )
  }

  //singleton
  companion object {
    @Volatile
    private var instance: GitHubApi? = null
    private const val baseUrl = "https://api.github.com/"

    fun getInstance(application: Application): GitHubApi? {
      if (instance == null) {
        println("API is null, creating new instance.")
        synchronized(GitHubApi::class.java) {
          instance = GitHubApi(application)
        }
      }
      return instance
    }
  }
}