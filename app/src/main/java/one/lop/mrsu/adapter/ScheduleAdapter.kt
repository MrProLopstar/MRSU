package one.lop.mrsu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import one.lop.mrsu.R
import one.lop.mrsu.model.TimeTable

class ScheduleAdapter(private var scheduleList: List<TimeTable>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val timeTable = scheduleList[position]
        holder.bind(timeTable)
    }

    override fun getItemCount(): Int = scheduleList.size

    fun updateData(newSchedule: List<TimeTable>) {
        scheduleList = newSchedule
        notifyDataSetChanged()
    }

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupText: TextView = itemView.findViewById(R.id.tv_group)
        private val facultyText: TextView = itemView.findViewById(R.id.tv_faculty)
        private val dateText: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(timeTable: TimeTable) {
            groupText.text = "Группа: ${timeTable.group}"
            facultyText.text = "Факультет: ${timeTable.facultyName}"
            dateText.text = "Дата: ${timeTable.timeTable.date}"
        }
    }
}
