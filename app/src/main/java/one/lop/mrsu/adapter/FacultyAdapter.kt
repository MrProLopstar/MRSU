package one.lop.mrsu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import one.lop.mrsu.R
import one.lop.mrsu.model.Discipline
import one.lop.mrsu.model.RecordBook

class FacultyAdapter(
    private val recordBooks: List<RecordBook>,
    private val onDisciplineClick: (Discipline) -> Unit
) : RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        return FacultyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
        val recordBook = recordBooks[position]
        holder.bind(recordBook)
    }

    override fun getItemCount(): Int = recordBooks.size

    inner class FacultyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val facultyTitle: TextView = itemView.findViewById(R.id.faculty_title)
        private val disciplineContainer: LinearLayout = itemView.findViewById(R.id.discipline_container)

        fun bind(recordBook: RecordBook) {
            facultyTitle.text = recordBook.faculty
            disciplineContainer.removeAllViews()

            recordBook.disciplines.forEach { discipline ->
                val disciplineView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_discipline, disciplineContainer, false)
                val disciplineTitle = disciplineView.findViewById<TextView>(R.id.discipline_title)

                disciplineTitle.text = discipline.title
                disciplineView.setOnClickListener { onDisciplineClick(discipline) }

                disciplineContainer.addView(disciplineView)
            }
        }
    }
}
