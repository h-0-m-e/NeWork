package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.crazylegend.imagepicker.pickers.SingleImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R
import ru.netology.nework.databinding.SignUpFragmentBinding
import ru.netology.nework.extentions.loadCircleFromLocalStorage
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.SignInUpViewModel
import java.io.File

class SignUpFragment: Fragment() {

    private val signViewModel: SignInUpViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val eventViewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SignUpFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        var pickedAvatar: AttachmentModel? = null

        signViewModel.dataState.observe(viewLifecycleOwner){state ->
            binding.loading.isVisible = state.loading
            binding.signUpButton.isVisible = !state.loading
            if (state.success && state.signedUp){
                postViewModel.loadPosts()
                eventViewModel.loadEvents()
                signViewModel.clean()
                findNavController().navigate( R.id.action_signUpFragment_to_feedFragment)
            }
        }

        binding.signUpTextButton.setOnClickListener {
            signViewModel.clean()
            findNavController().navigate( R.id.action_signUpFragment_to_signInFragment)
        }

        binding.avatar.setOnClickListener {
            SingleImagePicker.showPicker(context = this.requireActivity()){
                pickedAvatar = AttachmentModel(it.contentUri, File(it.contentUri.path!!))
                if(it.contentUri.toString().isNotBlank()) {
                    binding.avatar.loadCircleFromLocalStorage(it.contentUri)
                }
            }
        }
        binding.signUpButton.setOnClickListener{
            binding.incorrectError.isVisible = false
            binding.emptyError.isVisible = false
            binding.internetError.isVisible = false

            AndroidUtils.hideKeyboard(binding.root)

            val login = binding.login.text.toString().trim()
            val password = binding.password.text.toString()
            val name = binding.name.text.toString()

            if (login.isBlank() || password.isBlank() || name.isBlank()){
                binding.emptyError.isVisible = true
            } else if(!isOnline()){
                binding.internetError.isVisible = true
            } else {
                if (pickedAvatar != null){
                    signViewModel.signUp(login,password,name, pickedAvatar!!)
                } else{
                    MaterialAlertDialogBuilder(this.requireContext())
                        .setTitle(resources.getString(R.string.no_avatar))
                        .setMessage(resources.getString(R.string.no_avatar_msg))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                            SingleImagePicker.showPicker(context = this.requireContext()){
                                pickedAvatar = AttachmentModel(it.contentUri, it.contentUri.toFile())
                            }
                        }
                        .setPositiveButton(resources.getString(R.string.resume)) { _, _ ->
                            signViewModel.signUpWithNoAvatar(login,password,name)
                        }
                        .show()
                }
            }
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    signViewModel.clean()
                    findNavController().navigateUp()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )


        return binding.root
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            this.requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}
