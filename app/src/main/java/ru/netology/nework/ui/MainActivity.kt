package ru.netology.nework.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import ru.netology.nework.R
import ru.netology.nework.databinding.ActivityMainBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment,
                R.id.myWallFragment,
                R.id.notificationsFragment,
                R.id.createFragment -> bottomNav.visibility = View.VISIBLE

                else -> bottomNav.visibility = View.GONE
            }

        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.feed -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.feedFragment)
                    true
                }

                R.id.profile -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.myWallFragment)
                    true
                }

                R.id.notifications -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.notificationsFragment)
                    true
                }

                R.id.create -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.createFragment)
                    true
                }

                else -> false
            }
        }


        val authViewModel: AuthViewModel by viewModels()
        val postViewModel: PostViewModel by viewModels()
        val eventViewModel: EventViewModel by viewModels()

//        val postEventTypes = arrayOf("Post","Event")
//        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, postEventTypes)
//        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
//        autoCompleteTextView.setAdapter(arrayAdapter)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

//        if (authViewModel.isAuthorized){
//            postViewModel.refresh()
//            eventViewModel.refresh()
//            findNavController(R.id.nav_host_fragment).navigate(R.id.feedFragment)
//        }

    }


}

