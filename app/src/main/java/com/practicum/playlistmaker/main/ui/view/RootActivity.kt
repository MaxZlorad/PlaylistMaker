package com.practicum.playlistmaker.main.ui.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }

        // Находим NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        // Связываем BottomNavigationView с NavController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Скрываем BottomNavigationView при переходе на PlayerFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            //binding.bottomNavigationView.isVisible = destination.id != R.id.playerFragment
            val isVisible = destination.id != R.id.playerFragment
            binding.bottomNavigationView.isVisible = isVisible
            binding.dividerLine.isVisible = isVisible
        }

    }
}