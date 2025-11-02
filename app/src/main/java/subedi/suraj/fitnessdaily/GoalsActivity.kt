package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import subedi.suraj.fitnessdaily.model.FitnessGoal
import subedi.suraj.fitnessdaily.model.GoalType
import subedi.suraj.fitnessdaily.repository.DataRepository
import java.util.Calendar
import java.util.Date

class GoalsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddGoal: Button
    private lateinit var btnBack: Button
    private lateinit var btnTestData: Button
    private lateinit var progressCircle: ProgressBar
    private lateinit var tvGoalsProgress: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvDataTest: TextView
    private lateinit var weeklyProgressContainer: LinearLayout

    private val goalList = mutableListOf<FitnessGoal>()
    private var completedGoalsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadGoals()
        loadCompletedGoalsCount()
        updateAllData() // Moved after method definitions
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.goalRecyclerView)
        btnAddGoal = findViewById(R.id.btnAddGoal)
        btnBack = findViewById(R.id.btnBack)
        btnTestData = findViewById(R.id.btnTestData)
        progressCircle = findViewById(R.id.progressCircle)
        tvGoalsProgress = findViewById(R.id.tvGoalsProgress)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts)
        tvProtein = findViewById(R.id.tvProtein)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvFat = findViewById(R.id.tvFat)
        tvDataTest = findViewById(R.id.tvDataTest)
        weeklyProgressContainer = findViewById(R.id.weeklyProgressContainer)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GoalAdapter(goalList,
            onDeleteClick = { position ->
                showDeleteConfirmationDialog(position)
            },
            onEditClick = { position ->
                showEditGoalDialog(position)
            },
            onCompleteClick = { position ->
                markGoalAsCompleted(position)
            }
        )
    }

    private fun setupClickListeners() {
        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnTestData.setOnClickListener {
            updateAllData()
        }
    }

    private fun loadGoals() {
        goalList.clear()
        goalList.addAll(
            listOf(
                FitnessGoal(
                    title = "Weight Loss",
                    description = "Lose weight through exercise and diet",
                    targetValue = 5.0,
                    currentValue = calculateWeightLossProgress(),
                    unit = "kg",
                    goalType = GoalType.WEIGHT_LOSS,
                    isCompleted = false
                ),
                FitnessGoal(
                    title = "Monthly Workouts",
                    description = "Complete workouts this month",
                    targetValue = 20.0,
                    currentValue = DataRepository.getTotalWorkouts().toDouble(),
                    unit = "workouts",
                    goalType = GoalType.ENDURANCE,
                    isCompleted = false
                ),
                FitnessGoal(
                    title = "Calorie Deficit",
                    description = "Maintain daily calorie deficit",
                    targetValue = 500.0,
                    currentValue = calculateCalorieDeficit(),
                    unit = "calories",
                    goalType = GoalType.WEIGHT_LOSS,
                    isCompleted = false
                )
            )
        )
        recyclerView.adapter?.notifyDataSetChanged()
        updateOverallProgress()
    }

    private fun loadCompletedGoalsCount() {
        val sharedPreferences = getSharedPreferences("goals_data", MODE_PRIVATE)
        completedGoalsCount = sharedPreferences.getInt("completed_goals_count", 0)
    }

    private fun saveCompletedGoalsCount() {
        val sharedPreferences = getSharedPreferences("goals_data", MODE_PRIVATE)
        sharedPreferences.edit().putInt("completed_goals_count", completedGoalsCount).apply()
    }

    private fun calculateWeightLossProgress(): Double {
        val workouts = DataRepository.getTotalWorkouts()
        return (workouts * 0.1).coerceAtMost(5.0)
    }

    private fun calculateCalorieDeficit(): Double {
        val totalBurned = DataRepository.getTotalCaloriesBurned()
        val avgDailyCalories = DataRepository.getAverageDailyCalories()
        val maintenanceCalories = 2000

        val deficit = (maintenanceCalories * 30 - avgDailyCalories * 30 + totalBurned) / 30.0
        return deficit.coerceIn(0.0, 500.0)
    }

    // MOVE updateAllData() HERE - before it's called in onCreate
    private fun updateAllData() {
        updateStatistics()
        updateNutritionData()
        setupWeeklyProgressBars()
        runDataUsageTest()
        updateOverallProgress()
    }

    private fun updateStatistics() {
        val totalCaloriesBurned = DataRepository.getTotalCaloriesBurned()
        val totalWorkouts = DataRepository.getTotalWorkouts()

        tvTotalCalories.text = "Calories Burned: $totalCaloriesBurned"
        tvTotalWorkouts.text = "Workouts: $totalWorkouts"
    }

    private fun updateNutritionData() {
        val totalProtein = DataRepository.getTotalProtein()
        val totalCarbs = DataRepository.getTotalCarbs()
        val totalFat = DataRepository.getTotalFat()

        tvProtein.text = "Protein: ${String.format("%.1f", totalProtein)}g"
        tvCarbs.text = "Carbs: ${String.format("%.1f", totalCarbs)}g"
        tvFat.text = "Fat: ${String.format("%.1f", totalFat)}g"
    }

    private fun setupWeeklyProgressBars() {
        weeklyProgressContainer.removeAllViews()

        val weeklyData = calculateWeeklyProgress()

        for (weekData in weeklyData) {
            val weekLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }

            val weekLabel = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                text = weekData.label
                textSize = 14f
                setTextColor(Color.BLACK)
            }

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2.0f
                )
                progress = weekData.progress
                progressTintList = android.content.res.ColorStateList.valueOf(
                    when {
                        weekData.progress >= 80 -> Color.parseColor("#4CAF50")
                        weekData.progress >= 50 -> Color.parseColor("#FF9800")
                        else -> Color.parseColor("#F44336")
                    }
                )
            }

            val progressText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                text = "${weekData.progress}%"
                textSize = 14f
                gravity = android.view.Gravity.END
                setTextColor(
                    when {
                        weekData.progress >= 80 -> Color.parseColor("#4CAF50")
                        weekData.progress >= 50 -> Color.parseColor("#FF9800")
                        else -> Color.parseColor("#F44336")
                    }
                )
            }

            weekLayout.addView(weekLabel)
            weekLayout.addView(progressBar)
            weekLayout.addView(progressText)
            weeklyProgressContainer.addView(weekLayout)
        }
    }

    private fun calculateWeeklyProgress(): List<WeekData> {
        val workouts = DataRepository.getLast30DaysWorkouts()

        if (workouts.isEmpty()) {
            return listOf(
                WeekData("Week 1", 0),
                WeekData("Week 2", 0),
                WeekData("Week 3", 0),
                WeekData("Week 4", 0)
            )
        }

        val calendar = Calendar.getInstance()
        val weeklyWorkouts = mutableMapOf<String, Int>()

        workouts.forEach { workout ->
            calendar.time = workout.date
            val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
            val weekKey = "Week $weekNumber"
            weeklyWorkouts[weekKey] = weeklyWorkouts.getOrDefault(weekKey, 0) + 1
        }

        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val weeklyProgress = mutableListOf<WeekData>()

        for (i in 3 downTo 0) {
            val weekNum = currentWeek - i
            val weekKey = "Week $weekNum"
            val workoutsThisWeek = weeklyWorkouts[weekKey] ?: 0
            val progress = (workoutsThisWeek * 100) / 5
            weeklyProgress.add(WeekData("W${i+1}", progress.coerceAtMost(100)))
        }

        return weeklyProgress
    }

    private fun runDataUsageTest() {
        val testResults = StringBuilder()

        val workouts = DataRepository.getWorkouts().size
        val meals = DataRepository.getMeals().size
        val totalCalories = DataRepository.getTotalCaloriesBurned()
        val totalWorkouts = DataRepository.getTotalWorkouts()
        val completedGoals = completedGoalsCount

        testResults.append("Data Status:\n")
        testResults.append("• Workouts: $workouts\n")
        testResults.append("• Meals: $meals\n")
        testResults.append("• Calories Burned: $totalCalories\n")
        testResults.append("• Monthly Workouts: $totalWorkouts\n")
        testResults.append("• Completed Goals: $completedGoals\n")

        if (workouts == 0 && meals == 0) {
            testResults.append("\n💡 Add workouts and meals to see progress!")
        } else if (workouts > 0 && meals > 0) {
            testResults.append("\n✅ Data integrated successfully!")
        } else if (workouts > 0) {
            testResults.append("\n📊 Workout data loaded. Add meals for nutrition data.")
        } else {
            testResults.append("\n🍎 Meal data loaded. Add workouts for exercise data.")
        }

        tvDataTest.text = testResults.toString()
    }

    private fun showAddGoalDialog() {
        showGoalDialog(null)
    }

    private fun showEditGoalDialog(position: Int) {
        showGoalDialog(goalList[position], position)
    }

    private fun showGoalDialog(existingGoal: FitnessGoal?, position: Int? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val etGoalTitle = dialogView.findViewById<EditText>(R.id.etGoalTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etTarget = dialogView.findViewById<EditText>(R.id.etTarget)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrent)
        val etUnit = dialogView.findViewById<EditText>(R.id.etUnit)

        existingGoal?.let { goal ->
            etGoalTitle.setText(goal.title)
            etDescription.setText(goal.description)
            etTarget.setText(goal.targetValue.toString())
            etCurrent.setText(goal.currentValue.toString())
            etUnit.setText(goal.unit)
        }

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(if (existingGoal == null) "Set New Goal" else "Edit Goal")
            .setView(dialogView)
            .setPositiveButton(if (existingGoal == null) "Add" else "Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.setOnShowListener {
            val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val title = etGoalTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val target = etTarget.text.toString().trim()
                val current = etCurrent.text.toString().trim()
                val unit = etUnit.text.toString().trim()

                if (title.isNotEmpty() && target.isNotEmpty()) {
                    val goal = FitnessGoal(
                        title = title,
                        description = description.ifEmpty { "Personal fitness goal" },
                        targetValue = target.toDouble(),
                        currentValue = current.toDoubleOrNull() ?: 0.0,
                        unit = unit.ifEmpty { "units" },
                        goalType = existingGoal?.goalType ?: GoalType.WEIGHT_LOSS,
                        isCompleted = existingGoal?.isCompleted ?: false
                    )

                    if (position != null) {
                        goalList[position] = goal
                        recyclerView.adapter?.notifyItemChanged(position)
                    } else {
                        goalList.add(0, goal)
                        recyclerView.adapter?.notifyItemInserted(0)
                    }
                    updateOverallProgress()
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goalList[position].title}'?")
            .setPositiveButton("Delete") { _, _ ->
                goalList.removeAt(position)
                recyclerView.adapter?.notifyItemRemoved(position)
                updateOverallProgress()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun markGoalAsCompleted(position: Int) {
        val goal = goalList[position]

        if (!goal.isCompleted) {
            AlertDialog.Builder(this)
                .setTitle("Complete Goal")
                .setMessage("Mark '${goal.title}' as completed?")
                .setPositiveButton("Complete") { _, _ ->
                    // Mark goal as completed
                    goalList[position] = goal.copy(isCompleted = true)
                    recyclerView.adapter?.notifyItemChanged(position)

                    // Track achievement
                    completedGoalsCount++
                    saveCompletedGoalsCount()

                    // Notify DataRepository for achievement tracking
                    DataRepository.completeGoal()

                    // Show success message
                    showCompletionMessage(goal.title)

                    updateOverallProgress()
                    updateAllData()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        } else {
            // Option to mark as incomplete
            AlertDialog.Builder(this)
                .setTitle("Reopen Goal")
                .setMessage("Mark '${goal.title}' as incomplete?")
                .setPositiveButton("Reopen") { _, _ ->
                    goalList[position] = goal.copy(isCompleted = false)
                    recyclerView.adapter?.notifyItemChanged(position)
                    updateOverallProgress()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
    }

    private fun showCompletionMessage(goalTitle: String) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Goal Completed!")
            .setMessage("Congratulations! You've completed '$goalTitle'.\n\nThis achievement has been recorded in your progress!")
            .setPositiveButton("Awesome!", null)
            .create()
            .show()
    }

    private fun updateOverallProgress() {
        if (goalList.isEmpty()) {
            progressCircle.progress = 0
            tvGoalsProgress.text = "0%"
            return
        }

        val completedGoals = goalList.count { it.isCompleted }
        val totalGoals = goalList.size
        val completionPercentage = if (totalGoals > 0) (completedGoals * 100) / totalGoals else 0

        progressCircle.progress = completionPercentage
        tvGoalsProgress.text = "$completionPercentage%"

        when {
            completionPercentage >= 80 -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                tvGoalsProgress.setTextColor(Color.parseColor("#4CAF50"))
            }
            completionPercentage >= 50 -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                tvGoalsProgress.setTextColor(Color.parseColor("#FF9800"))
            }
            else -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
                tvGoalsProgress.setTextColor(Color.parseColor("#F44336"))
            }
        }
    }
}

data class WeekData(val label: String, val progress: Int)

class GoalAdapter(
    private val goals: List<FitnessGoal>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onCompleteClick: (Int) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalTitle: TextView = itemView.findViewById(R.id.tvGoalTitle)
        val goalDescription: TextView = itemView.findViewById(R.id.tvGoalDescription)
        val goalProgress: TextView = itemView.findViewById(R.id.tvGoalProgress)
        val goalType: TextView = itemView.findViewById(R.id.tvGoalType)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
        val completedBadge: TextView = itemView.findViewById(R.id.tvCompletedBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        val progress = ((goal.currentValue / goal.targetValue) * 100).toInt().coerceAtMost(100)

        holder.goalTitle.text = goal.title
        holder.goalDescription.text = goal.description
        holder.goalProgress.text = "Progress: ${goal.currentValue}/${goal.targetValue} ${goal.unit} ($progress%)"
        holder.goalType.text = "Type: ${goal.goalType.name}"
        holder.progressBar.progress = progress

        // Update UI based on completion status
        if (goal.isCompleted) {
            holder.btnComplete.text = "Completed ✓"
            holder.btnComplete.setBackgroundColor(Color.parseColor("#4CAF50"))
            holder.btnComplete.setTextColor(Color.WHITE)
            holder.completedBadge.visibility = View.VISIBLE
            holder.progressBar.progress = 100
            holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            holder.btnComplete.text = "Mark Complete"
            holder.btnComplete.setBackgroundColor(Color.parseColor("#2196F3"))
            holder.btnComplete.setTextColor(Color.WHITE)
            holder.completedBadge.visibility = View.GONE

            when {
                progress >= 100 -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                progress >= 50 -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                else -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
            }
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(position)
        }

        holder.btnComplete.setOnClickListener {
            onCompleteClick(position)
        }
    }

    override fun getItemCount(): Int = goals.size
}