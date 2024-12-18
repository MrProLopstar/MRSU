package one.lop.mrsu

import CacheManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import one.lop.mrsu.R
import one.lop.mrsu.adapter.ScheduleAdapter
import one.lop.mrsu.model.Group
import one.lop.mrsu.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessageView: TextView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var materialCalendarView: MaterialCalendarView
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var cacheManager: CacheManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cacheManager = CacheManager(requireContext())
        cacheManager.clearOldCache()

        initUI(view)
        scheduleAdapter = ScheduleAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = scheduleAdapter

        val currentDate = getCurrentDate()
        prefetchScheduleForRange(currentDate)
        loadScheduleForDate(currentDate)

        toggleGroup.check(R.id.btn_week)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_month -> loadCalendarView("month")
                    R.id.btn_week -> loadCalendarView("week")
                }
                loadScheduleForDate(currentDate)
            }
        }

        materialCalendarView.setOnDateChangedListener { _, date, _ ->
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.date)
            loadScheduleForDate(formattedDate)
        }

        setCalendarLocale(Locale.getDefault())
        materialCalendarView.setDateSelected(Calendar.getInstance(), true)
        loadCalendarView("week")
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.schedule_list)
        emptyMessageView = view.findViewById(R.id.empty_schedule_message)
        materialCalendarView = view.findViewById(R.id.material_calendar_view)
        toggleGroup = view.findViewById(R.id.toggle_group)
    }

    private fun prefetchScheduleForRange(centerDate: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            for (offset in -3..3) {
                calendar.time = dateFormat.parse(centerDate) ?: continue
                calendar.add(Calendar.DAY_OF_YEAR, offset)
                val date = dateFormat.format(calendar.time)

                if (cacheManager.getGroupsForDate(date) == null) {
                    try {
                        val response = RetrofitClient.apiInstance.getStudentTimeTable(date)
                        if (response.isSuccessful) {
                            response.body()?.let { groups ->
                                cacheManager.saveGroupsForDate(date, groups)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ScheduleFragment", "Failed to prefetch data for date $date: ${e.message}")
                    }
                }
            }
        }
    }

    private fun loadScheduleForDate(date: String) {
        val cachedGroups = cacheManager.getGroupsForDate(date)
        if (cachedGroups != null) {
            Log.d("ScheduleFragment", "Using cached data for date: $date")
            updateSchedule(cachedGroups)
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = RetrofitClient.apiInstance.getStudentTimeTable(date)
                    if (response.isSuccessful) {
                        val groups = response.body() ?: emptyList()
                        Log.d("ScheduleFragment", "API Response for date $date: ${groups.size} groups")
                        groups.forEach { group ->
                            Log.d("ScheduleFragment", "Group: $group")
                        }
                        cacheManager.saveGroupsForDate(date, groups)
                        updateSchedule(groups)
                    } else {
                        Log.w("ScheduleFragment", "No schedule available for date: $date")
                        showEmptyMessage()
                    }
                } catch (e: Exception) {
                    Log.e("ScheduleFragment", "Failed to load schedule for date $date: ${e.message}")
                    showEmptyMessage()
                }
            }
        }
    }

    private fun updateSchedule(groups: List<Group>) {
        val allLessons = groups.flatMap { group ->
            listOfNotNull(group.timeTable?.lessons).flatten() // Исправлено
        }

        if (allLessons.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            emptyMessageView.visibility = View.GONE
            scheduleAdapter.updateData(allLessons)
        } else {
            showEmptyMessage()
        }
    }


    private fun showEmptyMessage() {
        recyclerView.visibility = View.GONE
        emptyMessageView.visibility = View.VISIBLE
    }

    private fun setCalendarLocale(locale: Locale) {
        val dateFormat = SimpleDateFormat("MMMM yyyy", locale)
        materialCalendarView.setTitleFormatter { calendarDay ->
            dateFormat.format(calendarDay.date).replaceFirstChar { it.uppercase() }
        }
        materialCalendarView.state().edit()
            .setFirstDayOfWeek(Calendar.MONDAY)
            .commit()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun loadCalendarView(range: String) {
        when (range) {
            "month" -> materialCalendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
            "week" -> materialCalendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit()
        }
    }
}
