package com.kikimore.github_user_list.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.kikimore.api.data.GitHubApi
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.utils.Resource
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.utils.fetchViewModel
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
class ProfileActivity : AppCompatActivity() {

  private val userName by lazy { intent.getStringExtra(USER_NAME) }
  private val api by lazy { GitHubApi.getInstance(application) }
  private val viewModel by lazy { fetchViewModel { ProfileViewModel(api, userName) } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)
    setDefault()
    viewModel.getProfile()
    viewModel.getProfileState().onEach {
      if (it == null) return@onEach
      when (it.status) {
        Resource.Status.SUCCESS -> {
          isLoading(false)
          it.data?.also { profile -> setContent(profile) }
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
  }

  private fun setDefault() {
    // back button
    backImageView.setOnClickListener {
      onBackPressed()
      finish()
    }
    // avatar
    val placeHolder: String? = null
    Glide.with(this)
      .load(placeHolder)
      .placeholder(R.drawable.ic_user)
      .centerInside()
      .into(userImageView)
    // header
    headerTextView.text = userName
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
      viewModel.saveNote(noteEditText.text.toString())
    }
  }

  private fun setContent(profile: Profile) {
    // header
    headerTextView.text = if (profile.name.isNotEmpty()) profile.name else profile.login
    // avatar
    Glide.with(this)
      .load(profile.avatarUrl)
      .centerInside()
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
  }

  private fun isLoading(bool: Boolean = false) {
    progressBar.visibility = if (bool) View.VISIBLE else View.GONE
  }

  companion object {
    private const val USER_NAME = "userName"
    fun getIntent(context: Context, userName: String?): Intent {
      return Intent(context, ProfileActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(USER_NAME, userName)
      }
    }
  }
}