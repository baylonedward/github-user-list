package com.kikimore.github_user_list.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * Created by: ebaylon.
 * Created on: 29/08/2020.
 */
@ExperimentalCoroutinesApi
class ProfileViewModel(private val api: GitHubApi?, private val userName: String) :
  ViewModel() {
  private val profile = MutableStateFlow<Profile?>(null)
  private val profileState = MutableStateFlow<Resource<Profile>?>(null)
  private var currentDelay = 1000L
  private val delayFactor = 2

  private suspend fun retryCall(retryMethod: () -> Unit) {
    profileState.value = Resource.loading()
    delay(currentDelay)
    retryMethod()
    currentDelay *= delayFactor
  }

  fun getProfile() {
    profileState.value = Resource.loading()
    val retry = { getProfile() }
    api?.userRepository()?.getProfile(userName)
      ?.distinctUntilChanged()
      ?.retry()
      ?.onEach {
        profileState.value = it
        it.data?.also { data -> profile.value = data }
        if (it.status == Resource.Status.ERROR) {
          retryCall(retry)
        }
      }?.flowOn(Dispatchers.IO)
      ?.launchIn(viewModelScope)
  }

  fun getProfileState() = profileState

  fun saveNote(note: String) {
    profileState.value = Resource.loading()
    val finalNote = if (note.isEmpty()) null else note
    val newProfile = profile.value?.copy(note = finalNote)
    newProfile?.also {
      api?.userRepository()?.updateProfile(it)?.onEach { resource ->
        profileState.value = resource
      }?.launchIn(viewModelScope)
    }
  }
}