package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddWorkout: Button
    private val workoutList = mutableListOf<Workout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.workoutRecyclerView)
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout)

        setupRecyclerView()
        setupClickListeners()
        loadSampleWorkouts()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = WorkoutAdapter(workoutList) { position ->
            showDeleteConfirmationDialog(position)
        }
    }

    private fun setupClickListeners() {
        btnAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_workout, null)
        val etWorkoutName = dialogView.findViewById<EditText>(R.id.etWorkoutName)
        val etWorkoutDuration = dialogView.findViewById<EditText>(R.id.etWorkoutDuration)
        val etWorkoutCalories = dialogView.findViewById<EditText>(R.id.etWorkoutCalories)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etWorkoutName.text.toString().trim()
                val duration = etWorkoutDuration.text.toString().trim()
                val calories = etWorkoutCalories.text.toString().trim()

                if (name.isNotEmpty() && duration.isNotEmpty() && calories.isNotEmpty()) {
                    val newWorkout = Workout(
                        name = name,
                        duration = duration.toInt(),
                        caloriesBurned = calories.toInt()
                    )
                    workoutList.add(0, newWorkout)
                    recyclerView.adapter?.notifyItemInserted(0)
                    recyclerView.smoothScrollToPosition(0)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete '${workoutList[position].name}'?")
            .setPositiveButton("Delete") { _, _ ->
                workoutList.removeAt(position)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun loadSampleWorkouts() {
        workoutList.clear()
        workoutList.addAll(
            listOf(
                Workout("Morning Cardio", 45, 350),
                Workout("Strength Training", 60, 450),
                Workout("Full Body Workout", 50, 400)
            )
        )
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

// Simple data class - put this in the same file
data class Workout(
    val name: String,
    val duration: Int,
    val caloriesBurned: Int
)

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        val workoutDuration: TextView = itemView.findViewById(R.id.tvWorkoutDuration)
        val workoutCalories: TextView = itemView.findViewById(R.id.tvWorkoutCalories)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.workoutName.text = workout.name
        holder.workoutDuration.text = "⏱️ ${workout.duration} minutes"
        holder.workoutCalories.text = "🔥 ${workout.caloriesBurned} calories"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = workouts.size
}