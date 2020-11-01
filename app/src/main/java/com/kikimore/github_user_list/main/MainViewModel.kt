package com.kikimore.github_user_list.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.entities.user.UserAndProfile
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.main.users.UserListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class MainViewModel(private val api: GitHubApi) : ViewModel(), UserListAdapter.UserListStrategy {

  val userAndProfileState = MutableStateFlow<Resource<List<UserAndProfile?>>?>(null)
  val profileState = MutableStateFlow<Resource<Profile>?>(null)
  val searchResult = MutableStateFlow<String?>(null)
  val onSelect = MutableStateFlow(false)
  private val searchText = MutableStateFlow<String?>(null)
  private var initialPageSize = 0
  private var users: List<UserAndProfile?>? = null
  private var filteredUsers: List<UserAndProfile?>? = null
  private var selectedUserName: String? = null
  private var currentDelay = 1000L

  /**
   * Implementation of UserListAdapter.UserListStrategy
   */
  override fun hasUser(position: Int): Boolean = getUser(position) != null

  override fun getUsersCount() = filteredUsers?.size ?: 0

  override fun getUserName(position: Int) = getUser(position)?.login

  override fun getDetails(position: Int) = getUser(position)?.type

  override fun getAvatarUrl(position: Int) = getUser(position)?.avatarUrl

  override fun hasNote(position: Int): Boolean = getProfile(position)?.note != null

  override fun isFourth(position: Int): Boolean = (position + 1) % 4 == 0

  override fun onClick(position: Int): () -> Unit = {
    selectedUserName = getUser(position)?.login
    onSelect.value = true
    onSelect.value = false
  }

  private fun checkErrorIfCanRetry(errorMessage: String?): Boolean {
    return errorMessage?.let {
      when {
        errorMessage.contains(HTTP_RATE_LIMIT_ERROR_CODE.toString()) -> false
        else -> true
      }
    } ?: true
  }

  private suspend fun retryCall(retryMethod: suspend () -> Unit) {
    delay(currentDelay)
    currentDelay *= 2
    retryMethod()
  }

  private fun getUserProfile(position: Int) = filteredUsers?.get(position)

  private fun getProfile(position: Int): Profile? = getUserProfile(position)?.profile

  private fun addLoadingItem() {
    val newList = filteredUsers?.toMutableList()
    newList?.add(null)
    userAndProfileState.value = userAndProfileState.value?.copy(data = newList?.toList())
    users = newList?.toList()
    filteredUsers = newList?.toList()
  }

  private fun getUser(position: Int): User? = getUserProfile(position)?.user

  private fun loadUsers() {
    viewModelScope.launch {
      val retry: suspend () -> Unit = { loadUsers() }
      api.userRepository().loadUsers()
        .catch { userAndProfileState.value = Resource.error(it.message!!) }
        .collect {
          if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
            retryCall(retry)
          }
        }
    }
  }

  suspend fun observeSearch() {
    searchText
      .collect { word ->
        if (word == null) return@collect
        filteredUsers = if (word.isNotEmpty()) {
          users?.filter { userProfile ->
            val note = userProfile?.profile?.note?.toLowerCase(Locale.ROOT) ?: ""
            val userName = userProfile?.user?.login?.toLowerCase(Locale.ROOT) ?: ""
            val finalWord = word.toLowerCase(Locale.ROOT)
            userName.contains(finalWord, ignoreCase = true) || note.contains(
              finalWord,
              ignoreCase = true
            )
          }
        } else {
          users
        }
        searchResult.value = word
      }
  }

  suspend fun getUsers() {
    loadUsers()
    api.userRepository().getUsers().distinctUntilChanged()
      .collect {
        userAndProfileState.value = Resource.success(it)
        it.also { list ->
          if (initialPageSize == 0) initialPageSize = list.size
          users = list
          filteredUsers = list
        }
      }
  }

  suspend fun loadMoreUsers(position: Int) {
    if (initialPageSize == 0) return
    if (position == (users?.size
        ?: 0) - 1 && getUser(position) != null
    ) { // position is end of list
      addLoadingItem() // add loading item on list
      delay(1000L) // add delay to see loading item
      val user = api.userRepository().getLastUser().first() ?: return
      api.userRepository().loadUsers(user.id)
        .catch { userAndProfileState.value = Resource.error(it.message!!) }
        .collect()
    }
  }

  fun setSearch(word: String) {
    searchText.value = word
  }

  fun hasUsers() = api.userRepository().hasUser()

  suspend fun getProfile() {
    val retry: suspend () -> Unit = { getProfile() }
    selectedUserName?.also { userName ->
      api.userRepository().getProfile(userName)
        .distinctUntilChanged()
        .catch { profileState.value = Resource.error(it.message!!) }
        .collect {
          if (it.status == Resource.Status.SUCCESS) profileState.value = it
          if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
            retryCall(retry)
          }
        }
    }
  }

  suspend fun saveNote(note: String) {
    val finalNote = if (note.isEmpty()) null else note
    val newProfile = profileState.value?.data?.copy(note = finalNote)
    profileState.value = Resource.loading()
    newProfile?.also {
      profileState.value = api.userRepository().updateProfile(it).single()
    }
  }

  fun clearProfile() {
    profileState.value = null
    currentDelay = initialDelay
  }

  fun cancelJobs() {
    viewModelScope.cancel()
    currentDelay = initialDelay
  }

  companion object {
    private const val initialDelay = 1000L
    private const val HTTP_RATE_LIMIT_ERROR_CODE = 403
  }
}