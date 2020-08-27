package com.kikimore.api.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 25/07/2020.
 */

@ExperimentalCoroutinesApi
fun <T, A> performGetOperation(
  databaseQuery: () -> Flow<T>,
  networkCall: suspend () -> Resource<A>,
  saveCallResult: suspend (A) -> Unit,
  networkCallBack: (suspend (A) -> Unit)? = null
): Flow<Resource<T>> = channelFlow {
  send(Resource.loading())
  launch {
    databaseQuery.invoke().map { Resource.success(it) }.collect {
      send(it)
    }
  }
  val responseStatus = networkCall.invoke()
  when (responseStatus.status) {
    Resource.Status.SUCCESS -> {
      responseStatus.data?.also {
        saveCallResult(it)
        networkCallBack?.invoke(it)
      }
    }
    Resource.Status.ERROR -> send(Resource.error(responseStatus.message!!))
    else -> {
    }
  }
}.flowOn(Dispatchers.IO)