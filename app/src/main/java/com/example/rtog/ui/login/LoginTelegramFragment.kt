package com.example.rtog.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.rtog.R
import com.example.rtog.databinding.FragmentAuthLoginTelegramBinding
import com.example.rtog.ui.profile.ProfileFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

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
            val inputCode = binding.textInput.text.toString()
            if (inputCode.isEmpty()) {
                Toast.makeText(requireContext(), "Необходимо ввести код для входа", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val response = sendAPIAuthRequest(inputCode)
                if (response == null) {
                    Toast.makeText(requireContext(), "Произошла ошибка запроса", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val responseType = response.getString("type")
                when (responseType) {
                    "login" -> {
                        val sessionToken = response.getString("session_token") // нужно будет сохранить
                        val userData = response.getJSONObject("user_data");
                        val surname = userData.getString("surname")
                        val name = userData.getString("name")
                        val patronymic = userData.getString("patronymic")
                        (parentFragment?.parentFragment as? ProfileFragment)?.login()
                    }
                    "register" -> {
                        val sessionToken = response.getString("session_token") // нужно будет сохранить
                        findNavController().navigate(R.id.action_loginTelegramFragment_to_regFragment)
                    }
                    "error" -> {
                        Toast.makeText(requireContext(), "Код для входа недействителен", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Неизвестный ответ сервера авторизации", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }

    suspend fun sendAPIAuthRequest(code: String): JSONObject? {
        return withContext(Dispatchers.IO) {

            val url = HttpUrl.Builder()
                .scheme("https")
                .host("cavej376.xyz")
                .addPathSegment("rtog")
                .addPathSegment("v1")
                .addPathSegment("auth")
                .addPathSegment("telegram")
                .addQueryParameter("code", code)
                .build()

            val request = Request.Builder()
                .url(url)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
