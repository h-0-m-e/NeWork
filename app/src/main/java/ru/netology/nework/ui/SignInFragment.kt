package ru.netology.nework.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.netology.nework.R
import ru.netology.nework.databinding.SignInFragmentBinding
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.ConnectionChecker
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.SignInUpViewModel

class SignInFragment: Fragment() {

    private val signViewModel: SignInUpViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val eventViewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SignInFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        signViewModel.dataState.observe(viewLifecycleOwner){state ->
            binding.loading.isVisible = state.loading
            binding.signInButton.isVisible = !state.loading
            if (state.success && state.signedIn){
//                postViewModel.loadPosts()
//                eventViewModel.loadEvents()
                signViewModel.clean()
                findNavController().navigate(R.id.action_signInFragment_to_feedFragment)
            }
        }

        binding.signUpTextButton.setOnClickListener {
            signViewModel.clean()
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.signInButton.setOnClickListener{
            binding.incorrectError.isVisible = false
            binding.emptyError.isVisible = false
            binding.internetError.isVisible = false

            AndroidUtils.hideKeyboard(binding.root)

            val login = binding.login.text.toString().trim()
            val password = binding.password.text.toString()

            if (login.isBlank() || password.isBlank()){
                binding.emptyError.isVisible = true
            } else if(!isOnline()){
                binding.internetError.isVisible = true
            } else{
                signViewModel.signIn(login,password)
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

    fun isOnline(): Boolean {
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
