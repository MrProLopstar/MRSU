package one.lop.mrsu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import one.lop.mrsu.adapter.FacultyAdapter
import one.lop.mrsu.model.RecordBook
import one.lop.mrsu.network.RetrofitClient
import one.lop.mrsu.R

class DisciplineListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val recordBooks = mutableListOf<RecordBook>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_discipline_list, container, false)
        recyclerView = view.findViewById(R.id.discipline_recycler_view)

        setupRecyclerView()
        loadRecordBooks()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FacultyAdapter(recordBooks) { discipline ->
            navigateToDiscipline(discipline.id)
        }
    }

    private fun loadRecordBooks() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiInstance.getStudentSemester()
                if (response.isSuccessful) {
                    response.body()?.recordBooks?.let {
                        recordBooks.clear()
                        recordBooks.addAll(it)
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDiscipline(disciplineId: Int) {
        val fragment = DisciplineFragment().apply {
            arguments = Bundle().apply {
                putInt("disciplineId", disciplineId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }
}
