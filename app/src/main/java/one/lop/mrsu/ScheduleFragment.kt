package one.lop.mrsu

import CacheManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
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

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var currentDate = dateFormat.format(calendar.time)
    private var isLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cacheManager = CacheManager(requireContext())
        cacheManager.clearOldCache()

        initUI(view)
        setupCalendar()
        setupToggleButton()
        setupSwipeListener()

        scheduleAdapter = ScheduleAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = scheduleAdapter

        loadScheduleForDate(currentDate)
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.schedule_list)
        emptyMessageView = view.findViewById(R.id.empty_schedule_message)
        materialCalendarView = view.findViewById(R.id.material_calendar_view)
        toggleGroup = view.findViewById(R.id.toggle_group)
    }

    private fun setupCalendar() {
        materialCalendarView.state().edit()
            .setFirstDayOfWeek(Calendar.MONDAY)
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit()

        materialCalendarView.setTitleFormatter { day ->
            val month = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
                .format(day.date)
            month.replaceFirstChar { it.uppercase() }
        }

        materialCalendarView.setOnDateChangedListener { _, date, _ ->
            calendar.time = date.date
            currentDate = dateFormat.format(calendar.time)
            updateCurrentDate()
        }

        materialCalendarView.setDateSelected(calendar.time, true)
    }

    private fun setupToggleButton() {
        toggleGroup.check(R.id.btn_week)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_month -> {
                        materialCalendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.MONTHS)
                            .commit()
                        postCheckButton(R.id.btn_month)
                    }
                    R.id.btn_week -> {
                        materialCalendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.WEEKS)
                            .commit()
                        postCheckButton(R.id.btn_week)
                    }
                }
            }
        }
    }

    private fun postCheckButton(buttonId: Int) {
        toggleGroup.post {
            toggleGroup.check(buttonId)
        }
    }

    private fun setupSwipeListener() {
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalScroll = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                totalScroll += dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val itemCount = layoutManager.itemCount

                    if (firstVisibleItemPosition == 0 && totalScroll < 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        currentDate = dateFormat.format(calendar.time)
                        updateCurrentDate()
                    }
                    if (lastVisibleItemPosition == itemCount - 1 && totalScroll > 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        currentDate = dateFormat.format(calendar.time)
                        updateCurrentDate()
                    }

                    totalScroll = 0
                }
            }
        })
    }

    private fun updateCurrentDate() {
        materialCalendarView.clearSelection()
        materialCalendarView.setDateSelected(calendar.time, true)
        loadScheduleForDate(currentDate)
    }

    private fun loadScheduleForDate(date: String) {
        if (isLoading) return
        isLoading = true

        val cachedGroups = cacheManager.getGroupsForDate(date)
        if (cachedGroups != null) {
            updateSchedule(cachedGroups)
            isLoading = false
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = RetrofitClient.apiInstance.getStudentTimeTable(date)
                    if (response.isSuccessful) {
                        val groups = response.body() ?: emptyList()
                        cacheManager.saveGroupsForDate(date, groups)
                        updateSchedule(groups)
                    } else {
                        showEmptyMessage()
                    }
                } catch (e: Exception) {
                    emptyMessageView.text = getString(R.string.loading_error)
                    showEmptyMessage()
                }
                isLoading = false
            }
        }
    }

    private fun updateSchedule(groups: List<Group>) {
        val hasLessons = groups.any { group ->
            group.timeTable?.lessons?.isNotEmpty() == true
        }

        if (hasLessons) {
            recyclerView.visibility = View.VISIBLE
            emptyMessageView.visibility = View.GONE
            scheduleAdapter.updateData(groups)
        } else {
            showEmptyMessage()
        }
    }

    private fun showEmptyMessage() {
        Log.d("ScheduleFragment", "Empty message is being shown.")
        recyclerView.visibility = View.GONE
        emptyMessageView.text = getString(R.string.no_schedule)
        emptyMessageView.visibility = View.VISIBLE
    }
}
