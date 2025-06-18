package com.example.rtog.ui.profile

import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.rtog.databinding.FragmentProfileBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.launch
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var credentialManager: CredentialManager
    private val WEB_CLIENT_ID = "515288218978-74ptpamlt86vf8e77j2b9gd84pvdfq5p.apps.googleusercontent.com"

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        credentialManager = CredentialManager.create(requireContext())

        binding.btnSignIn.setOnClickListener {
            lifecycleScope.launch {
                if (!startGoogleSignIn(true)) { // первая попытка - только авторизованные
                    Toast.makeText(requireContext(), "Не найдены авторизованные аккаунты", Toast.LENGTH_SHORT).show()
                    if (!startGoogleSignIn(false))
                        Toast.makeText(
                            requireContext(),
                            "На устройстве нет аккаунта Google.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }
        }

        profileViewModel.text.observe(viewLifecycleOwner) {
            binding.textProfile.text = it
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun startGoogleSignIn(filterAuthorized: Boolean): Boolean {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterAuthorized)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                handleSignIn(result)

            } catch (e: GetCredentialException) {
                when (e) {
                    is NoCredentialException -> {
                        if (filterAuthorized) {
                            // Повторно пробуем показать все аккаунты
                            startGoogleSignIn(false)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "На устройстве нет аккаунта Google.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is GetCredentialCancellationException -> {
                        Toast.makeText(requireContext(), "Вход отменён пользователем", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.e("ProfileFragment", "Ошибка входа", e)
            } catch (e: NoCredentialException) {
                return false
            }
        return true
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val name = googleCredential.displayName
                val email = googleCredential.id
                val photoUrl = googleCredential.profilePictureUri

                binding.textProfile.text = name ?: "Имя не найдено"
                binding.tvUserEmail.text = email ?: "Email не найден"
                Glide.with(this).load(photoUrl).into(binding.ivUserPhoto)

            } catch (e: GoogleIdTokenParsingException) {
                Log.e("ProfileFragment", "Недопустимый Google ID токен", e)
                Toast.makeText(requireContext(), "Ошибка токена Google", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("ProfileFragment", "Неизвестный тип учетных данных")
            Toast.makeText(requireContext(), "Неожиданный формат данных", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
