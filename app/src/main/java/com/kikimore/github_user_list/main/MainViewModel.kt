package com.kikimore.github_user_list.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.entities.user.UserAndProfile
import com.kikimore.api.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel(private val api: GitHubApi) : ViewModel() {

  private val userAndProfileState = MutableStateFlow<Resource<List<UserAndProfile?>>?>(null)
  private val profileState = MutableStateFlow<Resource<Profile>?>(null)
  private val searchText = MutableStateFlow<String?>(null)
  private val searchResult = MutableStateFlow<String?>(null)
  private var initialPageSize = 0
  private var users: List<UserAndProfile?>? = null
  private var filteredUsers: List<UserAndProfile?>? = null
  private var selectedUserName: String? = null
  private var currentDelay = 1000L
  val onSelect = MutableStateFlow<Boolean?>(null)

  init {
    observeSearch()
  }

  private fun observeSearch() {
    searchText
      .debounce(500L)
      .onEach { word ->
        if (word == null) return@onEach
        filteredUsers = if (word.isNotEmpty()) {
          users?.filter { userProfile ->
            val note = userProfile?.profile?.note?.toLowerCase() ?: ""
            val userName = userProfile?.user?.login?.toLowerCase() ?: ""
            val finalWord = word.toLowerCase()
            userName.contains(finalWord, ignoreCase = true) || note.contains(
              finalWord,
              ignoreCase = true
            )
          }
        } else {
          users
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

  private suspend fun retryCall(retryMethod: suspend () -> Unit) {
    delay(currentDelay)
    retryMethod()
    currentDelay *= 2
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
    viewModelScope.launch {
      val retry: suspend () -> Unit = { getUsers() }
      api.userRepository().getUsers()
        .distinctUntilChanged()
        .catch { userAndProfileState.value = Resource.error(it.message!!) }
        .collect {
          userAndProfileState.value = it
          it.data?.also { list ->
            if (initialPageSize == 0) initialPageSize = list.size
            users = list
            filteredUsers = list
          }
          if (it.status == Resource.Status.ERROR && checkErrorIfCanRetry(it.message)) {
            retryCall(retry)
          }
        }
    }
  }

  suspend fun loadMoreUsers(position: Int) {
    if (initialPageSize == 0) return
    if (position == (users?.size
        ?: 0) - 1 && getUser(position) != null
    ) { // position is end of list
      addLoadingItem() // add loading item on list
      delay(2000L) // add delay to see loading item
      val user = api.userRepository().getLastUser().first() ?: return
      api.userRepository().getUsers(user.id)
        .distinctUntilChanged()
        .catch { userAndProfileState.value = Resource.error(it.message!!) }
        .collect {
          userAndProfileState.value = it
          it.data?.also { list ->
            users = list
            filteredUsers = list
          }
        }
    }
  }

  fun setSearch(word: String) {
    searchText.value = word
  }

  fun hasUsers() = api.userRepository().hasUser()

  fun getSearchResult() = searchResult

  fun getUsersState() = userAndProfileState

  fun getUsersCount() = filteredUsers?.size ?: 0

  fun getUserName(position: Int) = getUser(position)?.login

  fun getDetails(position: Int) = getUser(position)?.type

  fun getAvatarUrl(position: Int) = getUser(position)?.avatarUrl

  fun hasNote(position: Int): Boolean = getProfile(position)?.note != null

  fun isFourth(position: Int): Boolean = (position + 1) % 4 == 0

  fun onClick(position: Int): () -> Unit = {
    selectedUserName = getUser(position)?.login
    onSelect.value = true
    onSelect.value = null
  }

  fun getProfileState() = profileState

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

  fun saveNote(note: String) {
    val finalNote = if (note.isEmpty()) null else note
    val newProfile = profileState.value?.data?.copy(note = finalNote)
    profileState.value = Resource.loading()
    newProfile?.also {
      viewModelScope.launch {
        api.userRepository().updateProfile(it).collect { resource ->
          profileState.value = resource
        }
      }
    }
  }

  fun clearProfile() {
    profileState.value = null
  }

  companion object {
    private const val initialDelay = 1000L
    private const val HTTP_RATE_LIMIT_ERROR_CODE = 403
  }
}