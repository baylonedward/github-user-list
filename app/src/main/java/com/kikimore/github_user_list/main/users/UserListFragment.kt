package com.kikimore.github_user_list.main.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.main.MainViewModel
import com.kikimore.github_user_list.utils.fetchViewModel
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.fragment_user_list.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by: ebaylon.
 * Created on: 11/09/2020.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class UserListFragment : Fragment() {

  private val api by lazy { GitHubApi.getInstance(requireActivity().application) }
  private val viewModel by lazy { requireActivity().fetchViewModel { MainViewModel(api) } }
  private val listAdapter by lazy { UserListAdapter(viewModel) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.getUsers()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_user_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    shimmerViewContainer.startShimmer()
    setListAdapter(view.userListView)
    setSearchView(view.userSearchView)
    setObservers()
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
  }

  private fun setListAdapter(recyclerView: RecyclerView) {
    recyclerView.apply {
      layoutManager = LinearLayoutManager(this@UserListFragment.activity)
      adapter = listAdapter
    }
  }

  private fun setSearchView(searchView: SearchView) {
    // search view
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

  private fun isLoading(bool: Boolean = false) {
    view?.progressBar?.apply {
      visibility = if (bool) View.VISIBLE else View.GONE
    }
  }

  private fun hasData(bool: Boolean = false) {
    view?.also {
      if (bool) {
        shimmerViewContainer.stopShimmer()
        shimmerViewContainer.visibility = View.GONE
      } else {
        shimmerViewContainer.startShimmer()
        shimmerViewContainer.visibility = View.VISIBLE
      }
    }
  }
}