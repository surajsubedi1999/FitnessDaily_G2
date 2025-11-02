package subedi.suraj.fitnessdaily

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import subedi.suraj.fitnessdaily.repository.DataRepository

class MainActivity : AppCompatActivity() {

    private lateinit var btnWorkouts: Button
    private lateinit var btnNutrition: Button
    private lateinit var btnGoals: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before creating
        applySavedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize DataRepository with context for achievements
        DataRepository.initializeAppContext(this)

        initializeViews()
        setupNavigation()
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
}