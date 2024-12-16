package com.captchakaro.appy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.captchakaro.appy.databinding.ActivityCaptchaBinding
import com.google.android.material.snackbar.Snackbar

class captchaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptchaBinding
    private lateinit var sharedPreferences: android.content.SharedPreferences

    companion object {
        const val TOTAL_LIMIT_KEY = "TOTAL_LIMIT"
        const val EARNINGS_KEY = "TODAY_EARNINGS"
        const val MAX_LIMIT = 50
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptchaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Retrieve and display username
        val userName = sharedPreferences.getString("USER_NAME", "Guest")
        binding.Welcome.text = "Hey,\n$userName!"
        displayBalance()

        // Load and display current stats
        val totalLimit = sharedPreferences.getInt(TOTAL_LIMIT_KEY, 0)
        val earnings = sharedPreferences.getInt(EARNINGS_KEY, 0)

        updateUI(totalLimit, earnings)

        // Initialize the first captcha
        generateCaptureCode()

        // Navigate to Wallet
        binding.llwalet.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }

        // Submit button logic
        binding.btnSubmit.setOnClickListener {
            val input = binding.etInput.text.toString()
            val captureCode = binding.tvCaptureCode.text.toString()

            if (totalLimit >= MAX_LIMIT) {
                // User reached the max limit, show a message and prevent further actions
                showSnackbar("Daily limit reached. Come back tomorrow!")
                return@setOnClickListener
            }

            if (input == captureCode) {
                handleCorrectInput(totalLimit, earnings)
            } else {
                handleIncorrectInput()
            }
        }
    }

    private fun handleCorrectInput(totalLimit: Int, earnings: Int) {
        // Update values
        val newLimit = totalLimit + 1
        val newEarnings = earnings + 1

        // Save updated values to SharedPreferences
        with(sharedPreferences.edit()) {
            putInt(TOTAL_LIMIT_KEY, newLimit)
            putInt(EARNINGS_KEY, newEarnings)
            apply()
        }

        // Add points to wallet using BalanceManager
        BalanceManager.addPoints(this, 1)

        // Update UI with new values
        updateUI(newLimit, newEarnings)

        // Show success screen temporarily
        binding.mainWork.visibility = android.view.View.GONE
        binding.llRight.visibility = android.view.View.VISIBLE

        binding.ok.setOnClickListener {
            // Restore main work and hide success screen
            binding.mainWork.visibility = android.view.View.VISIBLE
            binding.llRight.visibility = android.view.View.GONE
            binding.etInput.text.clear()
            generateCaptureCode()
            displayBalance()
        }
    }

    private fun handleIncorrectInput() {
        // Show failure screen temporarily
        binding.mainWork.visibility = android.view.View.GONE
        binding.llWrong.visibility = android.view.View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.mainWork.visibility = android.view.View.VISIBLE
            binding.llWrong.visibility = android.view.View.GONE
            binding.etInput.text.clear()
            generateCaptureCode()
        }, 2000)
    }

    private fun generateCaptureCode() {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwx"
        val length = 6
        val randomString = (1..length)
            .map { characters.random() }
            .joinToString("")
        binding.tvCaptureCode.text = randomString
    }

    private fun updateUI(totalLimit: Int, earnings: Int) {
        binding.tvTotalLimit.text = "Total Limit Left\n${totalLimit}/$MAX_LIMIT"
        binding.tvEarnings.text = "Today Earnings\n${BalanceManager.getDailyEarnings(this)}"
        binding.tvBalance.text = BalanceManager.getBalance(this).toString()
    }

    private fun displayBalance() {
        val balance = BalanceManager.getBalance(this)
        binding.tvBalance.text = balance.toString()
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(resources.getColor(android.R.color.holo_red_dark))
        snackbar.setTextColor(resources.getColor(android.R.color.white))
        snackbar.show()
    }
}
