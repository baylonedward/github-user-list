package com.kikimore.github_user_list.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Created by: ebaylon.
 * Created on: 18/10/2020.
 */

fun showSnackBar(view: View?, message: String) {
  view?.also {
    Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
  }
}

fun showToast(context: Context?, message: String) {
  Toast.makeText(context, message, Toast.LENGTH_SHORT)?.show()
}