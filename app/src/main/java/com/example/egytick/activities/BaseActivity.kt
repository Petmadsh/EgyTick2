package com.example.egytick.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.example.egytick.R

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_base)
    }

    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        val color = if (errorMessage) {
            ContextCompat.getColor(this@BaseActivity, R.color.colorSnackBarError)
        } else {
            ContextCompat.getColor(this@BaseActivity, R.color.colorSnackBarSuccess)
        }

        Log.d("BaseActivity", "Snackbar color: $color")

        snackBarView.setBackgroundColor(color)
        snackBar.show()
    }

}
