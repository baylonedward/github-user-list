package com.kikimore.github_user_list.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.utils.fetchViewModel
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
class UsersActivity : AppCompatActivity() {
  private val api by lazy { GitHubApi.getInstance(application) }
  private val viewModel by lazy { fetchViewModel { UserViewModel(api) } }
  private val listAdapter by lazy { UserListAdapter(viewModel) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_users)
    viewModel.getUsers()
    setObservers()
    setListAdapter(userListView)
  }

  private fun setObservers() {
    viewModel.getUsersState().onEach {
      if (it == null) return@onEach
      when (it.status) {
        Resource.Status.SUCCESS -> {
          listAdapter.notifyDataSetChanged()
          isLoading(false)
          it.data.also { list ->
            hasData(list?.isNotEmpty() ?: false)
          }
        }
        Resource.Status.LOADING -> {
          isLoading(true)
        }
        Resource.Status.ERROR -> {
          println("Status: ${it.message}")
          isLoading(false)
          it.message?.let { it1 -> Snackbar.make(rootLayout, it1, Snackbar.LENGTH_LONG).show() }
        }
      }
    }.launchIn(lifecycleScope)
  }

  private fun setListAdapter(recyclerView: RecyclerView) {
    recyclerView.apply {
      layoutManager = LinearLayoutManager(this@UsersActivity)
      adapter = listAdapter
    }
  }

  private fun isLoading(bool: Boolean = false) {
    emptyTextView.text = LOADING
    progressBar.visibility = if (bool) View.VISIBLE else View.GONE
  }

  private fun hasData(bool: Boolean = false) {
    emptyTextView.text = NO_DATA
    emptyTextView.visibility = if (bool) View.INVISIBLE else View.VISIBLE
  }

  companion object {
    private const val LOADING = "Loading..."
    private const val NO_DATA = "No data to show."
  }
}