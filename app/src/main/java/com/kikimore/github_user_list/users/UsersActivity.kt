package com.kikimore.github_user_list.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
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

  override fun onResume() {
    super.onResume()
    shimmerViewContainer.startShimmer()
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
          isLoading(false)
          it.message?.let { it1 -> Snackbar.make(rootLayout, it1, Snackbar.LENGTH_LONG).show() }
        }
      }
    }.launchIn(lifecycleScope)
    // search result
    viewModel.getSearchResult().onEach {
      listAdapter.notifyDataSetChanged()
    }.launchIn(lifecycleScope)
    // search view
    userSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        query?.also { viewModel.setSearch(it) }
        return true
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        newText?.also { viewModel.setSearch(it) }
        return true
      }
    })
  }

  private fun setListAdapter(recyclerView: RecyclerView) {
    recyclerView.apply {
      layoutManager = LinearLayoutManager(this@UsersActivity)
      adapter = listAdapter
    }
  }

  private fun isLoading(bool: Boolean = false) {
    progressBar.visibility = if (bool) View.VISIBLE else View.GONE
  }

  private fun hasData(bool: Boolean = false) {
    if (bool) {
      shimmerViewContainer.stopShimmer()
      shimmerViewContainer.visibility = View.GONE
    } else {
      shimmerViewContainer.startShimmer()
      shimmerViewContainer.visibility = View.VISIBLE
    }
  }
}