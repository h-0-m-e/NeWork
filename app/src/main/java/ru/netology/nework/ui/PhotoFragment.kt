package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.netology.nework.R
import ru.netology.nework.databinding.PhotoFragmentBinding
import ru.netology.nework.utils.StringArg

class PhotoFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PhotoFragmentBinding.inflate(
            inflater,
            container,
            false
        )


        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}
