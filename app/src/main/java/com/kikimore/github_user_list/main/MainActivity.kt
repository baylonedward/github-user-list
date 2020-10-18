package com.kikimore.github_user_list.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.kikimore.api.data.GitHubApi
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.main.users.UserListFragmentDirections
import com.kikimore.github_user_list.utils.fetchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {
  private val api by lazy { GitHubApi.getInstance(application) }
  private val viewModel by lazy { fetchViewModel { MainViewModel(api) } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // observe selection to navigate
    viewModel.onSelect.onEach {
      if (it == null) return@onEach
      findNavController(R.id.nav_host_fragment).navigate(UserListFragmentDirections.navigationUserListToNavigationProfile())
    }.launchIn(lifecycleScope)
  }
}