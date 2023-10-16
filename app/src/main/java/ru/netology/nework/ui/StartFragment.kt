package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.netology.nework.R
import ru.netology.nework.databinding.StartFragmentBinding
import ru.netology.nework.viewmodel.AuthViewModel

class StartFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = StartFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        this.requireActivity()
            .findViewById<BottomNavigationView>(
                R.id.bottomNav
            )
            .visibility = View.GONE

        binding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_signInFragment)
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_signUpFragment)
        }

        return binding.root
    }
}
