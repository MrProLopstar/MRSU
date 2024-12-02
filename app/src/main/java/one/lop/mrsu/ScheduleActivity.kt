package one.lop.mrsu

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.model.TimeTable
import one.lop.mrsu.network.RetrofitClient
import one.lop.mrsu.adapter.ScheduleAdapter
import com.prolificinteractive.materialcalendarview.CalendarMode

class ScheduleActivity : AppCompatActivity() {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var dateTextView: TextView
    private lateinit var scheduleList: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Инициализация UI
        toggleGroup = findViewById(R.id.toggle_group)
        calendarView = findViewById(R.id.material_calendar_view)
        dateTextView = findViewById(R.id.tv_date)
        scheduleList = findViewById(R.id.schedule_list)

        // Устанавливаем адаптер для списка расписания
        scheduleAdapter = ScheduleAdapter(emptyList())
        scheduleList.layoutManager = LinearLayoutManager(this)
        scheduleList.adapter = scheduleAdapter

        // Обработчик изменения диапазона
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_month -> loadCalendarView("month")
                    R.id.btn_week -> loadCalendarView("week")
                    R.id.btn_day -> loadCalendarView("day")
                }
            }
        }

        // Обработчик выбора даты в календаре
        calendarView.setOnDateChangedListener(OnDateSelectedListener { _, date, _ ->
            val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
            dateTextView.text = "Дата: $selectedDate"
            loadScheduleForDate(selectedDate)
        })

        // Загрузка начального состояния
        loadCalendarView("day")
        loadScheduleForDate(getCurrentDate())
    }

    private fun loadCalendarView(range: String) {
        when (range) {
            "month" -> {
                calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit()
            }
            "week" -> {
                calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.WEEKS)
                    .commit()
            }
            "day" -> {
                calendarView.visibility = android.view.View.GONE
            }
        }
    }

    private fun loadScheduleForDate(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.getStudentTimeTable("Bearer your_token", date)
                if (response.isSuccessful && response.body() != null) {
                    val schedule = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        updateSchedule(schedule)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Ошибка загрузки расписания")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun updateSchedule(schedule: List<TimeTable>) {
        scheduleAdapter.updateData(schedule)
    }

    private fun showError(message: String) {
        dateTextView.text = message
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }
}
