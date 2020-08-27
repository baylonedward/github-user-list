package com.kikimore.api.data.remote

/**
 * Created by: ebaylon.
 * Created on: 27/08/2020.
 */
class UserRemoteDataSource(private val userService: UserService) : BaseDataResource() {

  suspend fun getUsersSince(since: Int = 0) = getResult { userService.getUsersSince(since) }
}