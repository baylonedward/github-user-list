package com.kikimore.github_user_list.main

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.entities.user.UserAndProfile
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.main.users.UserListFragmentDirections
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel(private val api: GitHubApi?) : ViewModel() {

  private val userAndProfileState = MutableStateFlow<Resource<List<UserAndProfile?>>?>(null)
  private val profileState = MutableStateFlow<Resource<Profile>?>(null)
  private val searchText = MutableStateFlow<String?>(null)
  private val searchResult = MutableStateFlow<String?>(null)
  private val delayFactor = 2
  private var initialPageSize = 0
  private var currentDelay = 1000L
  private var users: List<UserAndProfile?>? = null
  private var filteredUsers: List<UserAndProfile?>? = null
  private var profile: Profile? = null
  private var selectedUserName: String? = null

  init {
    observeSearch()
  }

  private fun observeSearch() {
    searchText
      .debounce(500L)
      .onEach { word ->
        if (word == null) return@onEach
        if (word.isNotEmpty()) {
          filteredUsers = users?.filter { userProfile ->
            val note = userProfile?.profile?.note?.toLowerCase() ?: ""
            val userName = userProfile?.user?.login?.toLowerCase() ?: ""
            val finalWord = word.toLowerCase()
            userName.contains(finalWord, ignoreCase = true) || note.contains(
              finalWord,
              ignoreCase = true
            )
          }
        } else {
          filteredUsers = users
        }
        searchResult.value = word
      }.launchIn(viewModelScope)
  }

  private fun checkErrorIfCanRetry(errorMessage: String?): Boolean {
    return errorMessage?.let {
      when {
        errorMessage.contains(HTTP_RATE_LIMIT_ERROR_CODE.toString()) -> false
        else -> true
      }
    } ?: true
  }

  private suspend fun retryCall(retryMethod: () -> Unit) {
    userAndProfileState.value = Resource.loading()
    delay(currentDelay)
    retryMethod()
    currentDelay *= delayFactor
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

  private fun removeLoadingItem() {
    val newList = filteredUsers?.toMutableList()
    newList?.removeAt(newList.size - 1)
    userAndProfileState.value = userAndProfileState.value?.copy(data = newList?.toList())
    users = newList?.toList()
    filteredUsers = newList?.toList()
  }

  fun getUser(position: Int): User? = getUserProfile(position)?.user

  fun getUsers() {
    val retry = { getUsers() }
    api?.userRepository()?.getUsers()
      ?.distinctUntilChanged()
      ?.catch { userAndProfileState.value = Resource.error(it.message!!) }
      ?.onEach {
        userAndProfileState.value = it
        it.data?.also { list ->
          if (initialPageSize == 0) initialPageSize = list.size
          users = list
          filteredUsers = list
        }
        if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
          retryCall(retry)
        }
      }?.launchIn(viewModelScope)
  }

  fun loadMoreUsers(position: Int) {
    if (initialPageSize == 0) return
    if (position == getUsersCount() - 1 && getUser(position) != null) { // position is end of list
      addLoadingItem() // add loading item on list
      val retry = { loadMoreUsers(position) }
      viewModelScope.launch {
        delay(2000L) // add delay to see loading item
        val user = api?.userRepository()?.getLastUser()?.first() ?: return@launch
        api.userRepository().getUsers(user.id)
          .distinctUntilChanged()
          .onEach {
            userAndProfileState.value = it
            if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
              retryCall(retry)
            }
            removeLoadingItem()
            cancel() // cancel job after first result
          }.launchIn(viewModelScope)
      }
    }
  }

  fun setSearch(word: String) {
    searchText.value = word
  }

  fun hasUsers() = users?.isNotEmpty() ?: false

  fun getSearchResult() = searchResult

  fun getUsersState() = userAndProfileState

  fun getUsersCount() = filteredUsers?.size ?: 0

  fun getUserName(position: Int) = getUser(position)?.login

  fun getDetails(position: Int) = getUser(position)?.type

  fun getAvatarUrl(position: Int) = getUser(position)?.avatarUrl

  fun hasNote(position: Int): Boolean = getProfile(position)?.note != null

  fun isFourth(position: Int): Boolean = (position + 1) % 4 == 0

  fun onClick(position: Int, view: View): () -> Unit = {
    selectedUserName = getUser(position)?.login
    view.findNavController()
      .navigate(UserListFragmentDirections.navigationUserListToNavigationProfile())
  }

  fun getProfileState() = profileState

  fun getProfile() {
    profileState.value = Resource.loading()
    val retry = { getProfile() }
    selectedUserName?.also { userName ->
      api?.userRepository()?.getProfile(userName)
        ?.distinctUntilChanged()
        ?.catch { profileState.value = Resource.error(it.message!!) }
        ?.onEach {
          profileState.value = it
          it.data?.also { data -> profile = data }
          if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
            retryCall(retry)
          }
        }?.launchIn(viewModelScope)
    }
  }

  fun saveNote(note: String) {
    profileState.value = Resource.loading()
    val finalNote = if (note.isEmpty()) null else note
    val newProfile = profile?.copy(note = finalNote)
    newProfile?.also {
      api?.userRepository()?.updateProfile(it)?.onEach { resource ->
        profileState.value = resource
      }?.launchIn(viewModelScope)
    }
  }

  companion object {
    private const val LIST_END_OFFSET = 10
    private const val HTTP_RATE_LIMIT_ERROR_CODE = 403
  }
}