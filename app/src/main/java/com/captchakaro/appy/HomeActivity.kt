package com.captchakaro.appy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.captchakaro.appy.databinding.ActivityHomeBinding
import android.widget.Toast
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences // Declare it as lateinit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize sharedPreferences property
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Retrieve the username from SharedPreferences
        val userName = sharedPreferences.getString("USER_NAME", "Guest")

        // Set the welcome message
        binding.Welcome.text = "Hey,\n$userName!"
        val currentBalance = BalanceManager.getBalance(this)
        binding.tvBalance.text = currentBalance.toString()

        binding.llwalet.setOnClickListener {
            startActivity(Intent(this, WithdrowActivity::class.java))
        }

        // Navigate to Give Test Activity on click
        binding.llGiveTest.setOnClickListener {
            startActivity(Intent(this, givetestActivity::class.java))
            markGiveTestCompleted() // Mark that the user has completed the test
        }

        // Navigate to Start Typing Activity on click (condition: Give Test completed today)
        binding.llStartTyping.setOnClickListener {
            if (isGiveTestCompletedToday()) {
                startActivity(Intent(this, captchaActivity::class.java))
            } else {
                Toast.makeText(this, "Please complete the Give Test first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mark the Give Test as completed for the current day
    private fun markGiveTestCompleted() {
        val editor = sharedPreferences.edit()
        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        editor.putInt("last_test_completed_day", currentDate) // Save the current day of the year
        editor.apply()
    }

    // Check if the Give Test has been completed today
    private fun isGiveTestCompletedToday(): Boolean {
        val lastCompletedDay = sharedPreferences.getInt("last_test_completed_day", -1)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return lastCompletedDay == currentDay
    }
}
