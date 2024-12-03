package one.lop.mrsu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Инфлейтим layout для HomeFragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // Метод для сброса состояния вкладки (например, прокрутка вверх)
    fun scrollToTop() {
        // Реализуйте логику сброса состояния, например, прокрутку RecyclerView вверх
    }

    // Метод для открытия других страниц внутри вкладки
    fun openOtherPage() {
        // Реализуйте открытие другой страницы внутри вкладки, возможно, с помощью вложенных фрагментов
    }
}
