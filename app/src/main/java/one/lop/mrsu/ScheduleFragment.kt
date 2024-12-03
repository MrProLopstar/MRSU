package one.lop.mrsu

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.adapter.ScheduleAdapter
import one.lop.mrsu.model.TimeTable
import one.lop.mrsu.network.RetrofitClient
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ScheduleFragment : Fragment() {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var dateTextView: TextView
    private lateinit var scheduleList: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var securePrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Инфлейтим layout для ScheduleFragment
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
        initUI(view)
        loadInitialData()
    }

    private fun initPreferences() {
        // Инициализация EncryptedSharedPreferences
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            requireContext(),
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun initUI(view: View) {
        toggleGroup = view.findViewById(R.id.toggle_group)
        calendarView = view.findViewById(R.id.material_calendar_view)
        dateTextView = view.findViewById(R.id.tv_date)
        scheduleList = view.findViewById(R.id.schedule_list)

        scheduleAdapter = ScheduleAdapter(emptyList())
        scheduleList.layoutManager = LinearLayoutManager(requireContext())
        scheduleList.adapter = scheduleAdapter

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_month -> loadCalendarView("month")
                    R.id.btn_week -> loadCalendarView("week")
                    R.id.btn_day -> loadCalendarView("day")
                }
            }
        }

        calendarView.setOnDateChangedListener(OnDateSelectedListener { _, date, _ ->
            val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
            dateTextView.text = "Дата: $selectedDate"
            loadScheduleForDate(selectedDate)
        })
    }

    private fun loadInitialData() {
        loadCalendarView("day")
        loadScheduleForDate(getCurrentDate())
    }

    private fun loadCalendarView(range: String) {
        when (range) {
            "month" -> {
                calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit()
                calendarView.visibility = View.VISIBLE
            }
            "week" -> {
                calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.WEEKS)
                    .commit()
                calendarView.visibility = View.VISIBLE
            }
            "day" -> {
                calendarView.visibility = View.GONE
            }
        }
    }

    private fun loadScheduleForDate(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = securePrefs.getString("access_token", null) ?: ""
                Log.d("ScheduleFragment", "Access token: $accessToken")
                if (accessToken.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        (activity as? BaseActivity)?.showToast(getString(R.string.error_token_missing))
                        (activity as? BaseActivity)?.navigateToLogin()
                    }
                    return@launch
                }

                val response = RetrofitClient.apiInstance.getStudentTimeTable("Bearer $accessToken", date)
                if (response.isSuccessful && response.body() != null) {
                    val schedule = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        updateSchedule(schedule)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError(getString(R.string.error_loading_schedule))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError(getString(R.string.error) + ": ${e.message}")
                }
            }
        }
    }

    private fun updateSchedule(schedule: List<TimeTable>) {
        scheduleAdapter.updateData(schedule)
    }

    private fun showError(message: String) {
        dateTextView.text = message
        (activity as? BaseActivity)?.showToast(message)
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    // Метод для сброса состояния вкладки (например, прокрутка вверх)
    fun scrollToTop() {
        scheduleList.scrollToPosition(0)
    }

    // Метод для открытия других страниц внутри вкладки
    fun openOtherPage() {
        // Реализуйте открытие другой страницы внутри вкладки, возможно, с помощью вложенных фрагментов
    }
}
