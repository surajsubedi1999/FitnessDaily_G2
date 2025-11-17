package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import subedi.suraj.fitnessdaily.repository.DataRepository
import subedi.suraj.fitnessdaily.utils.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var btnWorkouts: Button
    private lateinit var btnNutrition: Button
    private lateinit var btnGoals: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Check if tutorial should be shown FIRST
        val preferencesManager = PreferencesManager(this)
        if (preferencesManager.isFirstLaunch()) {
            startActivity(Intent(this, TutorialActivity::class.java))
            finish()
            return
        }

        // Apply theme before creating
        applySavedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize DataRepository with context for achievements
        DataRepository.initializeAppContext(this)

        initializeViews()
        setupNavigation()

        // Show motivational quote on startup
        showStartupQuote()
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun initializeViews() {
        btnWorkouts = findViewById(R.id.btnWorkouts)
        btnNutrition = findViewById(R.id.btnNutrition)
        btnGoals = findViewById(R.id.btnGoals)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupNavigation() {
        // Set default fragment
        showFragment(WorkoutFragment())

        btnWorkouts.setOnClickListener {
            showFragment(WorkoutFragment())
        }

        btnNutrition.setOnClickListener {
            showFragment(NutritionFragment())
        }

        btnGoals.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showStartupQuote() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lastStartupTime = sharedPreferences.getLong("last_startup_quote", 0)
        val currentTime = System.currentTimeMillis()
        val threeHoursInMillis = 3 * 60 * 60 * 1000

        // Show quote only once every 3 hours
        if (currentTime - lastStartupTime > threeHoursInMillis) {
            val quote = DataRepository.getRandomQuote()
            showSimpleQuoteDialog(quote)
            sharedPreferences.edit().putLong("last_startup_quote", currentTime).apply()
        }
    }

    private fun showSimpleQuoteDialog(quote: String) {
        AlertDialog.Builder(this)
            .setTitle("💪 Daily Motivation")
            .setMessage(quote)
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}