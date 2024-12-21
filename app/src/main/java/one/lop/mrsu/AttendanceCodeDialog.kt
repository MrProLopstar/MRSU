package one.lop.mrsu

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.network.RetrofitClient

class AttendanceCodeDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Optional: Customize dialog appearance
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_attendance_code, container, false)

        val etAttendanceCode = view.findViewById<EditText>(R.id.etAttendanceCode)
        val btnSubmitCode = view.findViewById<Button>(R.id.btnSubmitCode)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnSubmitCode.setOnClickListener {
            val code = etAttendanceCode.text.toString()
            if (code.isNotBlank()) {
                submitAttendanceCode(code)
            } else {
                Toast.makeText(requireContext(), getString(R.string.empty_code_error), Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun submitAttendanceCode(code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.postStudentAttendanceCode(code)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val attendance = response.body()
                        Toast.makeText(
                            requireContext(),
                            getString(
                                R.string.attendance_success_message,
                                attendance?.disciplineTitle,
                                attendance?.date
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.attendance_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
