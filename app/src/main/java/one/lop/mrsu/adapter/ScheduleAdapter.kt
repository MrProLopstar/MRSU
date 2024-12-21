package one.lop.mrsu.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import one.lop.mrsu.R
import one.lop.mrsu.model.Group
import one.lop.mrsu.model.Lesson
import one.lop.mrsu.DisciplineFragment

class ScheduleAdapter(private var groups: List<Group>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GROUP_HEADER = 0
        private const val VIEW_TYPE_LESSON = 1
        private const val VIEW_TYPE_EMPTY = 2

        private val LESSON_TIMES = listOf(
            "08:00 - 09:30",
            "09:45 - 11:15",
            "11:35 - 13:05",
            "13:20 - 14:50",
            "15:00 - 16:30",
            "16:40 - 18:10",
            "18:15 - 19:45",
            "19:50 - 21:20"
        )
    }

    private val items = mutableListOf<Any>()

    fun updateData(newGroups: List<Group>) {
        groups = newGroups.sortedWith(compareByDescending { group ->
            group.facultyName.contains("факультет", ignoreCase = true) ||
                    group.facultyName.contains("институт", ignoreCase = true)
        })
        items.clear()

        for (group in groups) {
            val lessons = group.timeTable?.lessons ?: emptyList()
            if (lessons.isNotEmpty()) {
                items.add(group) // Добавляем заголовок группы
                items.addAll(lessons) // Добавляем уроки группы
            }
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Group -> VIEW_TYPE_GROUP_HEADER
            is Lesson -> VIEW_TYPE_LESSON
            else -> VIEW_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GROUP_HEADER -> GroupHeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_header, parent, false)
            )
            else -> LessonViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_schedule, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupHeaderViewHolder -> holder.bind(items[position] as Group)
            is LessonViewHolder -> holder.bind(items[position] as Lesson)
        }
    }

    override fun getItemCount(): Int = items.size

    class GroupHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.tv_group_title)

        fun bind(group: Group) {
            groupTitle.text =
                "${itemView.context.getString(R.string.group_label)} ${group.group} - ${group.facultyName}"
        }
    }

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lessonNumber: TextView = itemView.findViewById(R.id.tv_lesson_number)
        private val lessonTime: TextView = itemView.findViewById(R.id.tv_lesson_time)
        private val lessonTitle: TextView = itemView.findViewById(R.id.tv_lesson_title)
        private val lessonTeacher: TextView = itemView.findViewById(R.id.tv_lesson_teacher)
        private val lessonAuditorium: TextView = itemView.findViewById(R.id.tv_lesson_auditorium)
        private val teacherPhoto: ImageView = itemView.findViewById(R.id.iv_teacher_photo)

        fun bind(lesson: Lesson) {
            val discipline = lesson.disciplines.firstOrNull()
            lessonNumber.text = lesson.number.toString()

            val lessonIndex = (lesson.number - 1).coerceIn(0, LESSON_TIMES.size - 1)
            lessonTime.text = LESSON_TIMES[lessonIndex]

            lessonTitle.text = discipline?.title ?: itemView.context.getString(R.string.no_title)
            lessonTeacher.text = formatName(discipline?.teacher?.fio)
            lessonAuditorium.text = itemView.context.getString(
                R.string.auditorium_template,
                discipline?.auditorium?.number ?: "—",
                discipline?.auditorium?.campusTitle ?: "—"
            )

            itemView.setOnClickListener {
                discipline?.id?.let { disciplineId ->
                    val activity = itemView.context as? AppCompatActivity
                    val fragment = DisciplineFragment().apply {
                        arguments = Bundle().apply {
                            putInt("disciplineId", disciplineId)
                        }
                    }
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.content_frame, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }

            Glide.with(itemView.context)
                .load(discipline?.teacher?.photo?.urlSmall)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .transform(CenterCrop(), RoundedCornersTransformation(28, 0))
                .into(teacherPhoto)
        }

        private fun formatName(name: String?): String {
            if (name.isNullOrEmpty()) return itemView.context.getString(R.string.no_teacher)
            return name.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
        }
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
