package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import subedi.suraj.fitnessdaily.model.FitnessGoal
import subedi.suraj.fitnessdaily.model.GoalType

class GoalsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddGoal: Button
    private lateinit var btnBack: Button
    private lateinit var progressCircle: ProgressBar
    private lateinit var tvGoalsProgress: TextView
    private val goalList = mutableListOf<FitnessGoal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        recyclerView = findViewById(R.id.goalRecyclerView)
        btnAddGoal = findViewById(R.id.btnAddGoal)
        btnBack = findViewById(R.id.btnBack)
        progressCircle = findViewById(R.id.progressCircle)
        tvGoalsProgress = findViewById(R.id.tvGoalsProgress)

        setupRecyclerView()
        setupClickListeners()
        loadSampleGoals()
        updateOverallProgress()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GoalAdapter(goalList) { position ->
            showDeleteConfirmationDialog(position)
        }
    }

    private fun setupClickListeners() {
        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        btnBack.setOnClickListener {
            finish() // Close activity and go back to MainActivity
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val etGoalTitle = dialogView.findViewById<EditText>(R.id.etGoalTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etTarget = dialogView.findViewById<EditText>(R.id.etTarget)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrent)
        val etUnit = dialogView.findViewById<EditText>(R.id.etUnit)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etGoalTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val target = etTarget.text.toString().trim()
                val current = etCurrent.text.toString().trim()
                val unit = etUnit.text.toString().trim()

                if (title.isNotEmpty() && target.isNotEmpty()) {
                    val newGoal = FitnessGoal(
                        title = title,
                        description = description.ifEmpty { "Personal fitness goal" },
                        targetValue = target.toDouble(),
                        currentValue = current.toDoubleOrNull() ?: 0.0,
                        unit = unit.ifEmpty { "units" },
                        goalType = GoalType.WEIGHT_LOSS
                    )
                    goalList.add(0, newGoal)
                    recyclerView.adapter?.notifyItemInserted(0)
                    updateOverallProgress()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
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

    private fun updateOverallProgress() {
        if (goalList.isEmpty()) {
            progressCircle.progress = 0
            tvGoalsProgress.text = "Set your first goal!"
            return
        }

        val totalProgress = goalList.sumOf { goal ->
            ((goal.currentValue / goal.targetValue) * 100).coerceAtMost(100.0)
        }
        val averageProgress = (totalProgress / goalList.size).toInt()

        progressCircle.progress = averageProgress
        tvGoalsProgress.text = "Overall Progress: $averageProgress%"

        // Change color based on progress
        when {
            averageProgress >= 80 -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
                tvGoalsProgress.setTextColor(0xFF4CAF50.toInt())
            }
            averageProgress >= 50 -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(0xFFFF9800.toInt())
                tvGoalsProgress.setTextColor(0xFFFF9800.toInt())
            }
            else -> {
                progressCircle.progressTintList = android.content.res.ColorStateList.valueOf(0xFFF44336.toInt())
                tvGoalsProgress.setTextColor(0xFFF44336.toInt())
            }
        }
    }

    private fun loadSampleGoals() {
        goalList.addAll(
            listOf(
                FitnessGoal(
                    title = "Lose Weight",
                    description = "Target weight loss goal",
                    targetValue = 75.0,
                    currentValue = 80.0,
                    unit = "kg",
                    goalType = GoalType.WEIGHT_LOSS
                ),
                FitnessGoal(
                    title = "Weekly Workouts",
                    description = "Complete workouts per week",
                    targetValue = 5.0,
                    currentValue = 3.0,
                    unit = "sessions",
                    goalType = GoalType.ENDURANCE
                ),
                FitnessGoal(
                    title = "Calorie Intake",
                    description = "Daily calorie target",
                    targetValue = 2000.0,
                    currentValue = 1800.0,
                    unit = "calories",
                    goalType = GoalType.WEIGHT_LOSS
                )
            )
        )
        recyclerView.adapter?.notifyDataSetChanged()
        updateOverallProgress()
    }
}

// CORRECTED GoalAdapter class with all required methods
class GoalAdapter(
    private val goals: List<FitnessGoal>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalTitle: TextView = itemView.findViewById(R.id.tvGoalTitle)
        val goalDescription: TextView = itemView.findViewById(R.id.tvGoalDescription)
        val goalProgress: TextView = itemView.findViewById(R.id.tvGoalProgress)
        val goalType: TextView = itemView.findViewById(R.id.tvGoalType)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
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

        // Change progress bar color based on completion
        when {
            progress >= 100 -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
            progress >= 50 -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(0xFFFF9800.toInt())
            else -> holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(0xFFF44336.toInt())
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = goals.size
}