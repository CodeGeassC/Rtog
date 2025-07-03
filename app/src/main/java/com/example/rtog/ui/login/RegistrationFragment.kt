package com.example.rtog.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rtog.databinding.FragmentAuthRegistrationBinding
import com.example.rtog.types.FullName
import com.example.rtog.ui.profile.ProfileViewModel

class RegistrationFragment : Fragment() {

    private var _binding: FragmentAuthRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAuthRegistrationBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val agreement = binding.checkBox2.isChecked
            val adult = binding.checkBox3.isChecked

            if (!agreement || !adult) {
                Toast.makeText(requireContext(), "Подтвердите согласие и возраст", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.inputSurname.text?.isEmpty() != false || binding.inputName.text?.isEmpty() != false) {
                Toast.makeText(requireContext(), "Введите фамилию и имя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val surname = binding.inputSurname.text.toString()
            val name = binding.inputName.text.toString()
            val patronymicText = binding.inputPatronymic.text
            val patronymic = if (patronymicText?.isEmpty() != false) null else patronymicText.toString()

            viewModel.userFullName.value = FullName(surname, name, patronymic)
            Toast.makeText(requireContext(), "Регистрация завершена", Toast.LENGTH_SHORT).show()
        }
    }

    /*suspend fun sendAPIRegisterRequest(sessionToken: String, username: FullName): JSONObject? {
        return withContext(Dispatchers.IO) {

            val url = HttpUrl.Builder()
                .scheme("https")
                .host("cavej376.xyz")
                .addPathSegment("rtog")
                .addPathSegment("v1")
                .addPathSegment("auth")
                .addPathSegment("register")
                .build()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $sessionToken")
                .post(RequestBody.create(null, ByteArray(0)))
                .build()

            val client = OkHttpClient()

            try {
                client.newCall(request).execute().use { response ->
                    //if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    return@withContext JSONObject(body)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
