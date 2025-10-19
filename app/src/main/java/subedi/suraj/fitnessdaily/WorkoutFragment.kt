package subedi.suraj.fitnessdaily

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import subedi.suraj.fitnessdaily.model.Workout
import subedi.suraj.fitnessdaily.repository.DataRepository
import java.util.Date

class WorkoutFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddWorkout: Button
    private lateinit var btnFavorites: Button
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
        btnFavorites = view.findViewById(R.id.btnFavorites)

        setupRecyclerView()
        setupClickListeners()
        loadWorkouts()
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

        btnFavorites.setOnClickListener {
            showFavoritesDialog()
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_workout, null)
        val etWorkoutName = dialogView.findViewById<EditText>(R.id.etWorkoutName)
        val etWorkoutDuration = dialogView.findViewById<EditText>(R.id.etWorkoutDuration)
        val etWorkoutCalories = dialogView.findViewById<EditText>(R.id.etWorkoutCalories)
        val cbFavorite = dialogView.findViewById<CheckBox>(R.id.cbFavorite)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.setOnShowListener {
            val addButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val name = etWorkoutName.text.toString().trim()
                val duration = etWorkoutDuration.text.toString().trim()
                val calories = etWorkoutCalories.text.toString().trim()

                if (name.isEmpty() || duration.isEmpty() || calories.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        val isFavorite = cbFavorite.isChecked
                        val newWorkout = Workout(
                            name = name,
                            duration = duration.toInt(),
                            caloriesBurned = calories.toInt(),
                            date = Date(),
                            isFavorite = isFavorite
                        )

                        workoutList.add(0, newWorkout)
                        DataRepository.addWorkout(newWorkout)

                        // Add to favorites templates if checked
                        if (isFavorite) {
                            DataRepository.addFavoriteTemplate(newWorkout)
                        }

                        recyclerView.adapter?.notifyItemInserted(0)
                        recyclerView.smoothScrollToPosition(0)
                        Toast.makeText(requireContext(), "Workout added successfully", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        alertDialog.show()
    }

    private fun showFavoritesDialog() {
        val favoriteTemplates = DataRepository.getFavoriteWorkoutTemplates()

        if (favoriteTemplates.isEmpty()) {
            Toast.makeText(requireContext(), "No favorite workout templates found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create adapter for the list
        val workoutNames = favoriteTemplates.map { "${it.name} (${it.duration}min, ${it.caloriesBurned}cal)" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, workoutNames)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Favorite Workout")
            .setAdapter(adapter) { dialog, which ->
                val selectedTemplate = favoriteTemplates[which]

                val newWorkout = Workout(
                    name = selectedTemplate.name,
                    duration = selectedTemplate.duration,
                    caloriesBurned = selectedTemplate.caloriesBurned,
                    date = Date(),
                    isFavorite = true
                )

                workoutList.add(0, newWorkout)
                DataRepository.addWorkout(newWorkout)
                recyclerView.adapter?.notifyItemInserted(0)
                recyclerView.smoothScrollToPosition(0)
                Toast.makeText(requireContext(), "Favorite workout added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val workout = workoutList[position]
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete '${workout.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                workoutList.removeAt(position)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun loadWorkouts() {
        workoutList.clear()
        workoutList.addAll(DataRepository.getWorkouts())
        if (workoutList.isEmpty()) {
            val sampleWorkouts = listOf(
                Workout(name = "Morning Cardio", duration = 45, caloriesBurned = 350, date = Date()),
                Workout(name = "Strength Training", duration = 60, caloriesBurned = 450, date = Date()),
                Workout(name = "Full Body Workout", duration = 50, caloriesBurned = 400, date = Date())
            )
            workoutList.addAll(sampleWorkouts)
            sampleWorkouts.forEach { DataRepository.addWorkout(it) }
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

class WorkoutAdapter(
    private var workouts: MutableList<Workout>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        val workoutDuration: TextView = itemView.findViewById(R.id.tvWorkoutDuration)
        val workoutCalories: TextView = itemView.findViewById(R.id.tvWorkoutCalories)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
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
        holder.workoutDuration.text = "Duration: ${workout.duration} min"
        holder.workoutCalories.text = "Calories: ${workout.caloriesBurned}"

        // Set favorite star icon based on this specific workout's favorite status
        updateFavoriteIcon(holder.btnFavorite, workout.isFavorite)

        // Favorite button click - toggle favorite status
        holder.btnFavorite.setOnClickListener {
            val currentFavoriteStatus = workout.isFavorite

            if (currentFavoriteStatus) {
                // Remove from favorites
                DataRepository.removeWorkoutFromAllFavorites(workout.name, workout.duration, workout.caloriesBurned)

                // Update ALL workouts with same details in the current list
                workouts.forEachIndexed { index, w ->
                    if (w.name == workout.name && w.duration == workout.duration && w.caloriesBurned == workout.caloriesBurned) {
                        workouts[index] = w.copy(isFavorite = false)
                        // Also update in DataRepository
                        DataRepository.updateWorkoutFavorite(w.id, false)
                    }
                }

                Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                // Add to favorites
                DataRepository.addFavoriteTemplate(workout)
                // Update this workout to favorite
                workouts[position] = workout.copy(isFavorite = true)
                // Also update in DataRepository
                DataRepository.updateWorkoutFavorite(workout.id, true)
                Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
            }

            // Refresh the entire list to update all stars
            notifyDataSetChanged()
        }

        // Delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    private fun updateFavoriteIcon(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(android.R.drawable.btn_star_big_on)
            button.contentDescription = "Remove from favorites"
        } else {
            button.setImageResource(android.R.drawable.btn_star_big_off)
            button.contentDescription = "Add to favorites"
        }
    }

    override fun getItemCount(): Int = workouts.size
}