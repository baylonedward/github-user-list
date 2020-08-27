package com.kikimore.api.data.remote

import com.kikimore.api.data.entities.user.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by: ebaylon.
 * Created on: 27/08/2020.
 */
interface UserService {

  @GET("users")
  suspend fun getUsersSince(@Query("since") since: Int): Response<List<User>>
}