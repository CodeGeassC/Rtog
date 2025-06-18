package com.example.rtog.ui.profile

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.rtog.databinding.FragmentSidenavProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentSidenavProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentSidenavProfileBinding.inflate(inflater, container, false)

        /*profileViewModel.text.observe(viewLifecycleOwner) {
            binding.textProfile.text = it
        }*/

        if (viewModel.registered)
            binding.viewSwitcher.displayedChild = 1

        return binding.root
    }

    fun login() {
        viewModel.registered = true
        binding.viewSwitcher.displayedChild = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
