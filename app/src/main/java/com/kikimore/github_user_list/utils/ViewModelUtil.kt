package com.kikimore.github_user_list.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Extension functions for typesafe call of view model factory.
 * Created 1 for fragment and 1 for activity call.
 * With this method we can call viewModel easily and typesafe.
 */

inline fun <reified T : ViewModel> Fragment.fetchViewModel(noinline creator: (() -> T)? = null): T {
  return if (creator == null)
    ViewModelProvider(this).get(T::class.java)
  else
    ViewModelProvider(this, ViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.fetchViewModel(noinline creator: (() -> T)? = null): T {
  return if (creator == null)
    ViewModelProvider(this).get(T::class.java)
  else
    ViewModelProvider(this, ViewModelFactory(creator)).get(T::class.java)
}

/**
 * Generic factory method for view model to avoid one factory per view model
 */
class ViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return creator() as T
  }
}