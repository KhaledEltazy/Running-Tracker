
package com.android.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.runningtracker.R
import com.android.runningtracker.databinding.FragmentSetupBinding
import com.android.runningtracker.util.Constants.KEY_FIRST_TIME_TOGGLE
import com.android.runningtracker.util.Constants.KEY_NAME
import com.android.runningtracker.util.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SetupFragment : Fragment() {
    private lateinit var binding : FragmentSetupBinding

    @Inject
    lateinit var sharedPref : SharedPreferences
    lateinit var toolbar : MaterialTextView

    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_setup,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstAppOpen){
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment,
            savedInstanceState,
            navOption)
        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(requireView(),"Please insert Name and weight",Snackbar.LENGTH_LONG).show()
            }
        }
    }

        private fun writePersonDataToSharedPref() : Boolean {
            val name = binding.etName.text.toString()
            val weight = binding.etWeight.text.toString()
            if (name.isEmpty() || weight.isEmpty()) {
                return false
            }
                sharedPref.edit()
                    .putString(KEY_NAME, name)
                    .putFloat(KEY_WEIGHT, weight.toFloat())
                    .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
                    .apply()
            /*
            val toolbarText = "let's go $name"
            toolbar = requireView().findViewById(R.id.tvToolbarTitle)
            requireActivity().apply {
                toolbar.text = toolbarText
            }*/
                return true
        }

}