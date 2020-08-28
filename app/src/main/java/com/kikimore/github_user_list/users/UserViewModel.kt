package com.kikimore.github_user_list.users

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.entities.user.UserAndProfile
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.profile.ProfileActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class UserViewModel(private val api: GitHubApi?) : ViewModel() {

  private val users = MutableStateFlow<List<UserAndProfile>?>(null)
  private val userAndProfileState = MutableStateFlow<Resource<List<UserAndProfile>>?>(null)
  private var initialPageSize = 0
  private var currentDelay = 1000L
  private val delayFactor = 2

  private fun getUser(position: Int): User? = users.value?.get(position)?.user

  private fun getProfile(position: Int): Profile? = users.value?.get(position)?.profile

  private suspend fun retryCall(retryMethod: () -> Unit) {
    userAndProfileState.value = Resource.loading()
    delay(currentDelay)
    retryMethod()
    currentDelay *= delayFactor
  }

  fun getUsers() {
    val retry = { getUsers() }
    api?.userRepository()?.getUsers()
      ?.distinctUntilChanged()
      ?.onEach {
        userAndProfileState.value = it
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
    userAndProfileState.value = Resource.loading()
    viewModelScope.launch {
      api?.userRepository()?.getLastUser()?.also { user ->
        api.userRepository().getUsers(user.id)
          .distinctUntilChanged()
          .onEach {
            if (it.status == Resource.Status.ERROR) {
              retryCall(retry)
            }
          }.launchIn(viewModelScope)
      }
    }
  }

  fun getUsersState() = userAndProfileState

  fun getUsersCount() = users.value?.size ?: 0

  fun getId(position: Int) = getUser(position)?.id

  fun getUserName(position: Int) = getUser(position)?.login

  fun getDetails(position: Int) = getUser(position)?.type

  fun getAvatarUrl(position: Int) = getUser(position)?.avatarUrl

  fun hasNote(position: Int): Boolean {
    return getProfile(position)?.note != null
  }

  fun isFourth(position: Int): Boolean = (position + 1) % 4 == 0

  fun onClick(position: Int, context: Context): () -> Unit = {
    val user = getUser(position)
    ProfileActivity.getIntent(context, user?.login).apply {
      context.startActivity(this)
    }
  }

  fun endOffset() = LIST_END_OFFSET

  companion object {
    private const val LIST_END_OFFSET = 10
  }
}