package com.android.runningtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.android.runningtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {
    private var yesListener : (()-> Unit)? = null

    fun setYesListener(listener : ()-> Unit){
        yesListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.baseline_delete_forever_24)
            .setPositiveButton("Yes"){a,b ->
                yesListener?.let { yes->
                    yes()
                }
            }
            .setNegativeButton("No"){dialogInterface,a ->
                dialogInterface.cancel()

            }
            .create()

    }
}