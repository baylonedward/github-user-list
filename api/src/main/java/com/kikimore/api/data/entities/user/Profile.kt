package com.kikimore.api.data.entities.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by: ebaylon.
 * Created on: 27/08/2020.
 */
@Entity(tableName = "profiles")
data class Profile(
  val login: String,
  @PrimaryKey
  val id: Int,
  @SerializedName("node_id") val nodeId: String,
  @SerializedName("avatar_url") val avatarUrl: String,
  @SerializedName("gravatar_id") val gravatarId: String,
  val url: String,
  @SerializedName("html_url") val htmlUrl: String,
  @SerializedName("followers_url") val followersUrl: String,
  @SerializedName("following_url") val followingUrl: String,
  @SerializedName("gists_url") val gistsUrl: String,
  @SerializedName("starred_url") val starredUrl: String,
  @SerializedName("subscriptions_url") val subscriptionsUrl: String,
  @SerializedName("organizations_url") val organizationsUrl: String,
  @SerializedName("repos_url") val reposUrl: String,
  @SerializedName("events_url") val eventsUrl: String,
  @SerializedName("received_events_url") val receivedEventsUrl: String,
  val type: String,
  @SerializedName("site_admin") val siteAdmin: Boolean,
  val name: String,
  val company: String?,
  val blog: String?,
  val location: String?,
  val email: String?,
  val hireable: String?,
  val bio: String?,
  @SerializedName("twitter_username") val twitterUsername: String?,
  @SerializedName("public_repos") val publicRepos: Int,
  @SerializedName("public_gists") val publicGists: Int,
  val followers: Int,
  val following: Int,
  @SerializedName("created_at") val createdAt: String,
  @SerializedName("updated_at") val updatedAt: String,
  val note: String? = null
)