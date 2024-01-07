package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.netology.nework.R
import ru.netology.nework.databinding.SettingsFragmentBinding

class SettingsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SettingsFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_myWallFragment)
        }


        return binding.root
    }
}
