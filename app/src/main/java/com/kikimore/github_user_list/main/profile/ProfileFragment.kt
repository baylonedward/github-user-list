package com.kikimore.github_user_list.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.main.MainViewModel
import com.kikimore.github_user_list.utils.fetchViewModel
import com.kikimore.github_user_list.utils.showSnackBar
import com.kikimore.github_user_list.utils.showToast
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by: ebaylon.
 * Created on: 11/09/2020.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class ProfileFragment : Fragment() {

  private val api by lazy { GitHubApi.getInstance(requireActivity().application) }
  private val viewModel by lazy { requireActivity().fetchViewModel { MainViewModel(api) } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      viewModel.getProfile()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_profile, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setDefault()
    viewModel.profileState.onEach {
      if (it == null) return@onEach
      when (it.status) {
        Resource.Status.SUCCESS -> {
          isLoading(false)
          it.data?.also { profile ->
            setContent(profile)
          }
          it.message?.also { message -> showToast(view.context, message) }
        }
        Resource.Status.LOADING -> {
          isLoading(true)
        }
        Resource.Status.ERROR -> {
          isLoading(false)
          it.message?.also { message -> showSnackBar(view, message) }
        }
      }
    }.launchIn(lifecycleScope)
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.clearProfile()
  }

  private fun setDefault() {
    // back button
    backImageView.setOnClickListener {
      findNavController().popBackStack()
    }
    // avatar
    val placeHolder: String? = null
    Glide.with(this)
      .load(placeHolder)
      .placeholder(R.drawable.ic_user)
      .centerInside()
      .into(userImageView)
    // header
    headerTextView.text = "User"
    // followers
    followersTextView.text = "Followers:"
    // following
    followingTextView.text = "Following:"
    // name
    nameTextView.text = "Name:"
    // company
    companyTextView.text = "Company:"
    // blog
    blogTextView.text = "Blog:"
    // save button
    saveButton.setOnClickListener {
      lifecycleScope.launch {
        viewModel.saveNote(noteEditText.text.toString())
      }
    }
    // disable note
    disableNote(true)
  }

  private fun setContent(profile: Profile) {
    // header
    headerTextView.text = if (profile.name.isNotEmpty()) profile.name else profile.login
    // avatar
    Glide.with(this)
      .load(profile.avatarUrl)
      .centerInside()
      .apply(RequestOptions.circleCropTransform())
      .into(userImageView)
    // followers
    followersTextView.text = "Followers: ${profile.followers}"
    // following
    followingTextView.text = "Following: ${profile.following}"
    // name
    nameTextView.text = "Name: ${profile.name}"
    // company
    companyTextView.text = "Company: ${profile.company}"
    // blog
    blogTextView.text = "Blog: ${profile.blog}"
    // notes
    noteEditText.setText(profile.note)
    // enable save button
    disableNote(false)
  }

  private fun disableNote(bool: Boolean) {
    noteEditText.isEnabled = !bool
    saveButton.isEnabled = !bool
  }

  private fun isLoading(bool: Boolean = false) {
    progressBar.visibility = if (bool) View.VISIBLE else View.GONE
  }
}