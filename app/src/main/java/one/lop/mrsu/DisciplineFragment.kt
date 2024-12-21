package one.lop.mrsu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import one.lop.mrsu.model.DisciplineInfo
import one.lop.mrsu.model.StudentRatingPlan
import one.lop.mrsu.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class DisciplineFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var ratingPlanLayout: LinearLayout
    private lateinit var totalScoreTextView: TextView

    private var disciplineId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disciplineId = arguments?.getInt("disciplineId") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_discipline, container, false)
        initUI(view)
        loadDisciplineData()
        return view
    }

    private fun initUI(view: View) {
        titleTextView = view.findViewById(R.id.discipline_title)
        ratingPlanLayout = view.findViewById(R.id.rating_plan_layout)
        totalScoreTextView = view.findViewById(R.id.total_score_text)
    }

    private fun loadDisciplineData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val disciplineResponse = RetrofitClient.apiInstance.getDiscipline(disciplineId)
                val ratingResponse = RetrofitClient.apiInstance.getStudentRatingPlan(disciplineId)

                if (disciplineResponse.isSuccessful && ratingResponse.isSuccessful) {
                    disciplineResponse.body()?.let { updateDisciplineInfo(it) }
                    ratingResponse.body()?.let { updateRatingPlan(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateDisciplineInfo(discipline: DisciplineInfo) {
        titleTextView.text = discipline.title
    }

    private fun updateRatingPlan(ratingPlan: StudentRatingPlan) {
        ratingPlanLayout.removeAllViews()

        val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        var totalScore = 0.0
        var maxScore = 0.0

        ratingPlan.sections.forEach { section ->
            val sectionLayout = layoutInflater.inflate(R.layout.item_section, ratingPlanLayout, false)
            val sectionTitle = sectionLayout.findViewById<TextView>(R.id.section_title)
            val pointsContainer = sectionLayout.findViewById<LinearLayout>(R.id.points_container)

            sectionTitle.text = section.title

            section.controlDots.forEach { dot ->
                val controlPointView = layoutInflater.inflate(
                    R.layout.item_control_point, pointsContainer, false
                )
                val title = controlPointView.findViewById<TextView>(R.id.control_point_title)
                val points = controlPointView.findViewById<TextView>(R.id.control_point_points)
                val date = controlPointView.findViewById<TextView>(R.id.control_point_date)

                if (!dot.title.isNullOrBlank()) {
                    title.isVisible = true
                    title.text = dot.title.replaceFirstChar { it.uppercaseChar() }
                } else {
                    title.isVisible = false
                }

                // Округление баллов до десятых
                val ball = dot.mark?.ball?.let { String.format("%.1f", it) } ?: "0.0"
                val maxBall = dot.maxBall?.let { String.format("%.1f", it) } ?: "0.0"

                points.text = getString(R.string.section_points, ball, maxBall)
                date.text = getString(
                    R.string.submission_deadline, dot.date?.let {
                        try {
                            dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it))
                        } catch (e: Exception) {
                            getString(R.string.not_specified)
                        }
                    } ?: getString(R.string.not_specified)
                )

                pointsContainer.addView(controlPointView)

                totalScore += dot.mark?.ball ?: 0.0
                maxScore += dot.maxBall ?: 0.0
            }

            ratingPlanLayout.addView(sectionLayout)
        }

        totalScoreTextView.text = getString(
            R.string.total_score,
            String.format("%.1f", totalScore),
            String.format("%.1f", maxScore)
        )
    }

}
