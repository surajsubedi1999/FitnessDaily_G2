package subedi.suraj.fitnessdaily

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupClickListeners()

        // Load default WorkoutFragment when app starts
        if (savedInstanceState == null) {
            loadWorkoutFragment()
        }
    }

    private fun setupClickListeners() {
        // Workout Tracker Button - navigates to WorkoutFragment
        findViewById<android.widget.Button>(R.id.btnWorkouts).setOnClickListener {
            loadWorkoutFragment()
        }

        // Nutrition Tracker Button - navigates to NutritionFragment
        findViewById<android.widget.Button>(R.id.btnNutrition).setOnClickListener {
            loadNutritionFragment()
        }
    }

    private fun loadWorkoutFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, WorkoutFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadNutritionFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, NutritionFragment())
            .addToBackStack(null)
            .commit()
    }
}