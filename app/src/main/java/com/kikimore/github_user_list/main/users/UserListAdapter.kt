package com.kikimore.github_user_list.main.users

import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kikimore.github_user_list.R
import com.kikimore.github_user_list.main.MainViewModel
import kotlinx.android.synthetic.main.layout_user_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class UserListAdapter(private val viewModel: MainViewModel) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var lastPosition = -1

  override fun getItemViewType(position: Int): Int {
    return if (viewModel.getUser(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    return if (viewType == VIEW_TYPE_ITEM) {
      val view = layoutInflater.inflate(R.layout.layout_user_item, parent, false)
      UserViewHolder(view)
    } else {
      val view = layoutInflater.inflate(R.layout.layout_user_item_loading, parent, false)
      LoadingViewHolder(view)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is UserViewHolder -> {
        val adapterPosition = holder.adapterPosition
        holder.onBind(
          viewModel.getAvatarUrl(adapterPosition),
          viewModel.getUserName(adapterPosition),
          viewModel.getDetails(adapterPosition),
          viewModel.hasNote(adapterPosition),
          viewModel.isFourth(adapterPosition),
          viewModel.onClick(adapterPosition, holder.itemView)
        )
        // animate on first appearance
        if (position > lastPosition) {
          holder.itemView.apply {
            animation = AnimationUtils.loadAnimation(
              context,
              R.anim.item_animation_from_bottom
            )
            startAnimation(animation)
          }
          lastPosition = position
        }
      }
      is LoadingViewHolder -> {
      }
    }
  }

  override fun getItemCount(): Int {
    return viewModel.getUsersCount()
  }

  companion object {
    private const val VIEW_TYPE_ITEM = 0
    private const val VIEW_TYPE_LOADING = 1
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

  class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}