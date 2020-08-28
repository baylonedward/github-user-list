package com.kikimore.github_user_list.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class UserViewModel(private val api: GitHubApi?) : ViewModel() {

  private val users = MutableStateFlow<List<User>?>(null)
  private val usersState = MutableStateFlow<Resource<List<User>>?>(null)
  private var initialPageSize = 0
  private var currentDelay = 1000L
  private val delayFactor = 2

  private fun getUser(position: Int): User? = users.value?.get(position)

  private suspend fun retryCall(retryMethod: () -> Unit) {
    usersState.value = Resource.loading()
    delay(currentDelay)
    retryMethod()
    currentDelay *= delayFactor
  }

  fun getUsers() {
    val retry = { getUsers() }
    api?.userRepository()?.getUsers()
      ?.distinctUntilChanged()
      ?.retry()
      ?.onEach {
        usersState.value = it
        it.data?.also { list ->
          if (initialPageSize == 0) initialPageSize = list.size
          users.value = list
        }
        if (it.status == Resource.Status.ERROR) {
          retryCall(retry)
        }
      }?.launchIn(viewModelScope)
  }

  fun loadMoreUsers() {
    if (initialPageSize == 0) return
    val retry = { loadMoreUsers() }
    usersState.value = Resource.loading()
    viewModelScope.launch {
      api?.userRepository()?.getLastUser()?.also { user ->
        api.userRepository().getUsers(user.id)
          .distinctUntilChanged()
          .retry()
          .onEach {
            if (it.status == Resource.Status.ERROR) {
              retryCall(retry)
            }
          }.launchIn(viewModelScope)
      }
    }
  }

  fun getUsersState() = usersState

  fun getUsersCount() = users.value?.size ?: 0

  fun getId(position: Int) = getUser(position)?.id

  fun getUserName(position: Int) = getUser(position)?.login

  fun getDetails(position: Int) = getUser(position)?.type

  fun getAvatarUrl(position: Int) = getUser(position)?.avatarUrl

  fun hasNote(position: Int): Boolean {
    var hasNotes = false
    val user = getUser(position) ?: return false
    viewModelScope.launch {
      hasNotes = api?.userRepository()?.hasProfileNote(user.id) ?: false
    }
    return hasNotes
  }

  fun isFourth(position: Int): Boolean = (position + 1) % 4 == 0

  fun onClick(position: Int): () -> Unit = {

  }

  fun endOffset() = LIST_END_OFFSET

  companion object {
    private const val LIST_END_OFFSET = 10
  }
}