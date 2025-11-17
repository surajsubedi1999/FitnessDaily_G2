package subedi.suraj.fitnessdaily

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import subedi.suraj.fitnessdaily.model.TutorialPage
import subedi.suraj.fitnessdaily.utils.PreferencesManager

class TutorialActivity : AppCompatActivity() {

    private lateinit var rvTutorial: RecyclerView
    private lateinit var layoutDots: LinearLayout
    private lateinit var btnSkip: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnGetStarted: Button

    private lateinit var preferencesManager: PreferencesManager

    private val tutorialPages = listOf(
        TutorialPage(
            "Welcome to FitnessDaily",
            "Your personal fitness companion to help you achieve your health goals and transform your lifestyle."
        ),
        TutorialPage(
            "Track Your Workouts",
            "Log your exercises, monitor calories burned, and build impressive workout streaks to stay motivated."
        ),
        TutorialPage(
            "Monitor Nutrition",
            "Easily log your meals, track calories, and monitor macronutrients for balanced nutrition."
        ),
        TutorialPage(
            "Achieve Your Goals",
            "Set personalized fitness goals, track your progress, and celebrate your achievements along the way."
        ),
        TutorialPage(
            "Share Your Journey",
            "Share your progress, streaks, and achievements with friends to stay accountable and inspired."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        preferencesManager = PreferencesManager(this)
        initializeViews()
        setupTutorial()
        setupClickListeners()
        updateNavigationButtons(0)
    }

    private fun initializeViews() {
        rvTutorial = findViewById(R.id.rvTutorial)
        layoutDots = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btnSkip)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnGetStarted = findViewById(R.id.btnGetStarted)
    }

    private fun setupTutorial() {
        val adapter = TutorialAdapter(tutorialPages)
        rvTutorial.adapter = adapter
        rvTutorial.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        PagerSnapHelper().attachToRecyclerView(rvTutorial)

        createDotsIndicator(0)

        rvTutorial.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentPosition = getCurrentPage()
                createDotsIndicator(currentPosition)
                updateNavigationButtons(currentPosition)
            }
        })
    }

    private fun setupClickListeners() {
        btnSkip.setOnClickListener {
            completeTutorial()
        }

        btnPrevious.setOnClickListener {
            val currentPosition = getCurrentPage()
            if (currentPosition > 0) {
                rvTutorial.smoothScrollToPosition(currentPosition - 1)
            }
        }

        btnNext.setOnClickListener {
            val currentPosition = getCurrentPage()
            if (currentPosition < tutorialPages.size - 1) {
                rvTutorial.smoothScrollToPosition(currentPosition + 1)
            } else {
                completeTutorial()
            }
        }

        btnGetStarted.setOnClickListener {
            completeTutorial()
        }
    }

    private fun getCurrentPage(): Int {
        val layoutManager = rvTutorial.layoutManager as LinearLayoutManager
        return layoutManager.findFirstVisibleItemPosition()
    }

    private fun createDotsIndicator(currentPosition: Int) {
        layoutDots.removeAllViews()

        for (i in tutorialPages.indices) {
            val dot = View(this)
            val size = resources.getDimensionPixelSize(R.dimen.dot_size)
            val margin = resources.getDimensionPixelSize(R.dimen.dot_margin)

            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setSize(size, size)

            if (i == currentPosition) {
                shape.setColor(ContextCompat.getColor(this, android.R.color.holo_purple))
            } else {
                shape.setColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }

            dot.background = shape

            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            layoutDots.addView(dot, params)
        }
    }

    private fun updateNavigationButtons(currentPosition: Int) {
        btnPrevious.visibility = if (currentPosition > 0) View.VISIBLE else View.GONE

        if (currentPosition == tutorialPages.size - 1) {
            btnNext.visibility = View.GONE
            btnGetStarted.visibility = View.VISIBLE
        } else {
            btnNext.visibility = View.VISIBLE
            btnGetStarted.visibility = View.GONE
        }
    }

    private fun completeTutorial() {
        preferencesManager.setTutorialCompleted()
        preferencesManager.setFirstLaunchCompleted()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private inner class TutorialAdapter(
        private val tutorialPages: List<TutorialPage>
    ) : RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

        inner class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.tvTutorialTitle)
            val description: TextView = itemView.findViewById(R.id.tvTutorialDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
            val view = layoutInflater.inflate(R.layout.item_tutorial, parent, false)
            return TutorialViewHolder(view)
        }

        override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
            val page = tutorialPages[position]
            holder.title.text = page.title
            holder.description.text = page.description
        }

        override fun getItemCount(): Int = tutorialPages.size
    }
}