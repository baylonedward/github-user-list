package com.kikimore.github_user_list.users

import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kikimore.github_user_list.R
import kotlinx.android.synthetic.main.layout_user_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@ExperimentalCoroutinesApi
class UserListAdapter(private val viewModel: UserViewModel) :
  RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val view = layoutInflater.inflate(R.layout.layout_user_item, parent, false)
    return UserViewHolder(view)
  }

  override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
    val adapterPosition = holder.adapterPosition
    holder.onBind(
      viewModel.getAvatarUrl(adapterPosition),
      viewModel.getUserName(adapterPosition),
      viewModel.getDetails(adapterPosition),
      viewModel.hasNote(adapterPosition),
      viewModel.isFourth(adapterPosition),
      viewModel.onClick(adapterPosition, holder.itemView.context)
    )

    // if position = end offset we call method to load more data.
    if (adapterPosition == viewModel.getUsersCount() - viewModel.endOffset()) {
      viewModel.loadMoreUsers()
    }
  }

  override fun getItemCount(): Int {
    return viewModel.getUsersCount()
  }

  class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun onBind(
      avatarUrl: String?,
      userName: String?,
      details: String?,
      hasNote: Boolean,
      isFourth: Boolean,
      onClick: () -> Unit
    ) {
      itemView.apply {
        // user name
        userNameTextView.text = userName
        // details
        detailTextView.text = details
        // has note
        noteImageView.visibility = if (hasNote) View.VISIBLE else View.INVISIBLE
        // avatar
        Glide.with(itemView)
          .load(avatarUrl)
          .centerInside()
          .placeholder(R.drawable.ic_user)
          .into(userImageView)
        // is fourth
        if (isFourth) userImageView.colorFilter =
          ColorMatrixColorFilter(NEGATIVE) else userImageView.clearColorFilter()
        // on click
        userCardView.setOnClickListener { onClick() }
      }
    }

    companion object {
      /**
       * Color matrix that flips the components (`-1.0f * c + 255 = 255 - c`)
       * and keeps the alpha intact.
       */
      private val NEGATIVE = floatArrayOf(
        -1.0f, 0f, 0f, 0f, 255f, // red
        0f, -1.0f, 0f, 0f, 255f, // green
        0f, 0f, -1.0f, 0f, 255f, // blue
        0f, 0f, 0f, 1.0f, 0f //alpha
      )
    }
  }
}