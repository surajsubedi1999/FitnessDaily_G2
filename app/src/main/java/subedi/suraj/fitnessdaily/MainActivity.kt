package subedi.suraj.fitnessdaily

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var btnWorkouts: Button
    private lateinit var btnNutrition: Button
    private lateinit var btnGoals: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnWorkouts = findViewById(R.id.btnWorkouts)
        btnNutrition = findViewById(R.id.btnNutrition)
        btnGoals = findViewById(R.id.btnGoals)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Set default fragment (Workouts)
        showFragment(WorkoutFragment())

        btnWorkouts.setOnClickListener {
            showFragment(WorkoutFragment())
        }

        btnNutrition.setOnClickListener {
            showFragment(NutritionFragment())
        }

        btnGoals.setOnClickListener {
            // Launch GoalsActivity
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}