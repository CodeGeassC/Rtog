package com.example.rtog.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rtog.databinding.FragmentRegistrationBinding

class RegFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.button3.setOnClickListener {
            val agreement = binding.checkBox2.isChecked
            val adult = binding.checkBox3.isChecked

            if (agreement && adult) {
                Toast.makeText(requireContext(), "Регистрация завершена", Toast.LENGTH_SHORT).show()
                // Тут будет переход в основное приложение
            } else {
                Toast.makeText(requireContext(), "Подтвердите согласие и возраст", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
