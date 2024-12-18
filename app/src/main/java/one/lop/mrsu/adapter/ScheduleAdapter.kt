package one.lop.mrsu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import one.lop.mrsu.R
import one.lop.mrsu.model.Lesson
import one.lop.mrsu.model.Discipline

class ScheduleAdapter(private var lessons: List<Lesson>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    fun updateData(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged() // Уведомляем RecyclerView об обновлении данных
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lessonNumber: TextView = itemView.findViewById(R.id.tv_lesson_number)
        private val lessonTitle: TextView = itemView.findViewById(R.id.tv_lesson_title)
        private val lessonTeacher: TextView = itemView.findViewById(R.id.tv_lesson_teacher)

        fun bind(lesson: Lesson) {
            val discipline = lesson.disciplines.firstOrNull()
            if (discipline != null) {
                lessonNumber.text = "Урок №${lesson.number}"
                lessonTitle.text = discipline.title ?: "Без названия"
                lessonTeacher.text = discipline.teacher?.fio ?: "Преподаватель не указан"
            } else {
                lessonNumber.text = "Урок №${lesson.number}"
                lessonTitle.text = "Дисциплина отсутствует"
                lessonTeacher.text = "Преподаватель отсутствует"
            }
        }
    }
}
