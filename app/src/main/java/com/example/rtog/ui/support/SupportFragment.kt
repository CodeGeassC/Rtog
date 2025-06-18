package com.example.rtog.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rtog.databinding.FragmentSidenavSupportBinding

class SupportFragment : Fragment() {

    private var _binding: FragmentSidenavSupportBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val supportViewModel =
            ViewModelProvider(this).get(SupportViewModel::class.java)

        _binding = FragmentSidenavSupportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSupport
        supportViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}