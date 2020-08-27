package com.kikimore.api.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.kikimore.api.AndroidTestSetup
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User
import com.kikimore.api.data.local.ProfileDao
import com.kikimore.api.data.local.UserDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
@RunWith(AndroidJUnit4::class)
class GitHubDatabaseTest {

  private lateinit var testSetup: AndroidTestSetup
  private lateinit var userDao: UserDao
  private lateinit var profileDao: ProfileDao
  private lateinit var users: List<User>
  private lateinit var profile: Profile

  @Before
  fun start() {
    testSetup = AndroidTestSetup()
    userDao = testSetup.userDao()
    profileDao = testSetup.profileDao()

    // fetch data from json files
    runBlocking(Dispatchers.IO) {
      // users
      val userListString = testSetup.readJsonFile("users.json")
      val userType = object : TypeToken<List<User>>() {}.type
      users = GsonBuilder().create().fromJson<List<User>>(userListString, userType)
      // profile
      val profileString = testSetup.readJsonFile("profile.json")
      val profileType = object : TypeToken<Profile>() {}.type
      profile = GsonBuilder().create().fromJson<Profile>(profileString, profileType)

    }
  }

  @Test
  fun insertSingleUserTest() {
    runBlocking(Dispatchers.IO) {
      // Insert Single
      launch {
        val user = users[0]
        userDao.insert(user)
        userDao.all().collect {
          assertEquals(1, it.size)
          assertEquals(user, it[0])
          cancel()
        }
      }
    }
  }

  @Test
  fun insertMultipleUserTest() {
    runBlocking(Dispatchers.IO) {
      // Insert Multiple
      launch {
        userDao.insert(users)
        userDao.all().collect {
          assertEquals(users.size, it.size)
          cancel()
        }
      }
    }
  }

  @Test
  fun updateProfileTest() {
    runBlocking(Dispatchers.IO) {
      val newLocation = "Los Santos"
      val insertedProfile = CompletableDeferred<Profile>()
      launch {
        profileDao.insert(profile)
        profileDao.get(profile.id).collect {
          assertEquals(profile.location, it.location)
          insertedProfile.complete(it)
          cancel()
        }
      }
      launch {
        val updatedProfile = insertedProfile.await().copy(location = newLocation)
        profileDao.update(updatedProfile)
        profileDao.get(updatedProfile.id).collect {
          assertNotEquals(profile.location, it.location)
          assertEquals(newLocation, it.location)
          cancel()
        }
      }
    }
  }

  @Test
  fun insertProfileTest() {
    runBlocking(Dispatchers.IO) {
      launch {
        profileDao.insert(profile)
        profileDao.all().collect {
          assertEquals(1, it.size)
          assertEquals(profile, it[0])
          cancel()
        }
      }
    }
  }
}