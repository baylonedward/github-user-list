package com.kikimore.api

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.kikimore.api.data.local.GitHubDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by: ebaylon.
 * Created on: 28/08/2020.
 */
class AndroidTestSetup {
  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val db = Room.inMemoryDatabaseBuilder(context, GitHubDatabase::class.java).build()

  fun userDao() = db.userDao()
  fun profileDao() = db.profileDao()

  @Throws(IOException::class)
  suspend fun readJsonFile(filename: String): String? {
    return withContext(Dispatchers.IO) {
      val inputStream =
        InstrumentationRegistry.getInstrumentation().context.assets.open(filename)
      val br = BufferedReader(InputStreamReader(inputStream))
      val sb = StringBuilder()
      var line: String? = br.readLine()
      while (line != null) {
        sb.append(line)
        line = br.readLine()
      }
      sb.toString()
    }
  }
}