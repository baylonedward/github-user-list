package com.kikimore.api.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kikimore.api.data.entities.user.Profile
import com.kikimore.api.data.entities.user.User

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */

@Database(
  entities = [User::class, Profile::class],
  version = 4,
  exportSchema = false
)
abstract class GitHubDatabase : RoomDatabase() {

  abstract fun userDao(): UserDao
  abstract fun profileDao(): ProfileDao

  companion object {
    @Volatile
    private var instance: GitHubDatabase? = null

    fun getDatabase(context: Context): GitHubDatabase =
      instance ?: synchronized(this) { instance ?: buildDatabase(context).also { instance = it } }

    private fun buildDatabase(appContext: Context) =
      Room.databaseBuilder(appContext, GitHubDatabase::class.java, "gitHubDatabase")
        .fallbackToDestructiveMigration()
        .build()
  }
}