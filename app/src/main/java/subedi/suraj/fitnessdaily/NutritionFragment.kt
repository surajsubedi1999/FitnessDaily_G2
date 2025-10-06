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
import subedi.suraj.fitnessdaily.model.Meal
import subedi.suraj.fitnessdaily.model.MealType

class NutritionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddMeal: Button
    private lateinit var btnBackToWorkout: Button
    private val mealList = mutableListOf<Meal>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.mealRecyclerView)
        btnAddMeal = view.findViewById(R.id.btnAddMeal)
        btnBackToWorkout = view.findViewById(R.id.btnBackToWorkout)

        setupRecyclerView()
        setupClickListeners()
        loadSampleMeals()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MealAdapter(mealList) { position ->
            showDeleteConfirmationDialog(position)
        }
    }

    private fun setupClickListeners() {
        btnAddMeal.setOnClickListener {
            showAddMealDialog()
        }

        btnBackToWorkout.setOnClickListener {
            // Go back to WorkoutFragment
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun showAddMealDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_meal, null)
        val etMealName = dialogView.findViewById<EditText>(R.id.etMealName)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)
        val etProtein = dialogView.findViewById<EditText>(R.id.etProtein)
        val etCarbs = dialogView.findViewById<EditText>(R.id.etCarbs)
        val etFat = dialogView.findViewById<EditText>(R.id.etFat)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Meal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etMealName.text.toString().trim()
                val calories = etCalories.text.toString().trim()
                val protein = etProtein.text.toString().trim()
                val carbs = etCarbs.text.toString().trim()
                val fat = etFat.text.toString().trim()

                if (name.isNotEmpty() && calories.isNotEmpty()) {
                    val newMeal = Meal(
                        name = name,
                        calories = calories.toInt(),
                        protein = protein.toDoubleOrNull() ?: 0.0,
                        carbs = carbs.toDoubleOrNull() ?: 0.0,
                        fat = fat.toDoubleOrNull() ?: 0.0,
                        mealType = MealType.LUNCH
                    )
                    mealList.add(0, newMeal)
                    recyclerView.adapter?.notifyItemInserted(0)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete '${mealList[position].name}'?")
            .setPositiveButton("Delete") { _, _ ->
                mealList.removeAt(position)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun loadSampleMeals() {
        mealList.addAll(
            listOf(
                Meal(
                    name = "Chicken Salad",
                    calories = 350,
                    protein = 30.0,
                    carbs = 20.0,
                    fat = 12.0,
                    mealType = MealType.LUNCH
                ),
                Meal(
                    name = "Protein Shake",
                    calories = 200,
                    protein = 25.0,
                    carbs = 15.0,
                    fat = 5.0,
                    mealType = MealType.SNACK
                )
            )
        )
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

class MealAdapter(
    private val meals: List<Meal>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealName: TextView = itemView.findViewById(R.id.tvMealName)
        val mealCalories: TextView = itemView.findViewById(R.id.tvMealCalories)
        val mealMacros: TextView = itemView.findViewById(R.id.tvMealMacros)
        val mealType: TextView = itemView.findViewById(R.id.tvMealType)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.name
        holder.mealCalories.text = "Calories: ${meal.calories}"
        holder.mealMacros.text = "P: ${meal.protein}g C: ${meal.carbs}g F: ${meal.fat}g"
        holder.mealType.text = "Type: ${meal.mealType}"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = meals.size
}