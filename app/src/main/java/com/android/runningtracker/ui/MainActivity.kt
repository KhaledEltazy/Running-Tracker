package com.android.runningtracker.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.runningtracker.R
import com.android.runningtracker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        setSupportActionBar(binding.toolbar)

        binding.btnNavView.setupWithNavController(binding.fragmentsContainer.findNavController())

        binding.fragmentsContainer.findNavController()
            .addOnDestinationChangedListener{controller,destination,arguments ->
                when(destination.id){
                    R.id.settingFragment,R.id.staticFragment,R.id.runFragment ->
                        binding.btnNavView.visibility = View.VISIBLE
                    else -> binding.btnNavView.visibility = View.GONE
                }
            }
    }
}