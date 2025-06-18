package com.example.rtog.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rtog.R
import com.example.rtog.databinding.FragmentAuthLoginTelegramBinding

class LoginTelegramFragment : Fragment() {

    private var _binding: FragmentAuthLoginTelegramBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAuthLoginTelegramBinding.inflate(inflater, container, false)

        binding.btnConfirm.setOnClickListener {
            // Пока просто переходим на регистрацию
            findNavController().navigate(R.id.action_loginTelegramFragment_to_regFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
