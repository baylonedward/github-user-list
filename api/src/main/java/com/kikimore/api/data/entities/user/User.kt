package com.kikimore.api.data.entities.user

import com.google.gson.annotations.SerializedName

/**
 * Created by: ebaylon.
 * Created on: 27/08/2020.
 */
data class User(
  val login: String,
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
  @SerializedName("site_admin") val site_admin: Boolean
)