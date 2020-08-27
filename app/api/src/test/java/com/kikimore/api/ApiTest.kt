package com.kikimore.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Created by: ebaylon.
 * Created on: 27/08/2020.
 */

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class ApiTest {

  private lateinit var testSetup: ApiTestSetup

  @Before
  fun setup() {
    testSetup = ApiTestSetup()
  }

  @Test
  fun getCharacterSince() {
    val since = 0
    runBlocking(Dispatchers.IO) {
      testSetup.userService().getUsersSince(since)?.also {
        assertNotNull(it.body())
        assertNotEquals(0, it.body()?.size)
      }
    }
  }
}