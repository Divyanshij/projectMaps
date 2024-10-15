package com.example.collegeproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.collegeproject.ui.theme.CollegeprojectTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val signInButton = findViewById<Button>(R.id.button)
        signInButton.setOnClickListener(){
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()){
                val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs",
                    MODE_PRIVATE
                )
                val editor = sharedPreferences.edit()
                editor.putBoolean("isSignedIn",true)
                editor.apply()

                val homeIntent = Intent(this,MapsActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }
    }
}