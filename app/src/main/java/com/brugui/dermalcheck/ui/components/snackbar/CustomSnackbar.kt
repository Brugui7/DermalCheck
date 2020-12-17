package com.brugui.dermalcheck.ui.components.snackbar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.brugui.dermalcheck.R
import com.google.android.material.snackbar.BaseTransientBottomBar

class CustomSnackbar (
        parent: ViewGroup,
        content: CustomSnackbarView
) : BaseTransientBottomBar<CustomSnackbar>(parent, content, content) {


    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {

        @JvmStatic fun make(view: View,
                 message : String, duretion : Int,
                 listener : View.OnClickListener?, icon : Int, action_label : String?, bg_color : Int): CustomSnackbar? {

            // Finds a suitable parent for the custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                    "No suitable parent found from the given view. Please provide a valid view."
            )

            // Inflates the custom view
            try{
                val customView = LayoutInflater.from(view.context).inflate(
                        R.layout.custom_snackbar_inflation_view,
                        parent,
                        false
                ) as CustomSnackbarView
                // Creates and returns the Snackbar
                customView.tvMsg.text = message
                action_label?.let {
                    customView.tvAction.text = action_label
                    customView.tvAction.setOnClickListener {
                        listener?.onClick(customView.tvAction)
                    }
                }
                customView.imLeft.setImageResource(icon)
                customView.layRoot.setBackgroundColor(bg_color)


                return CustomSnackbar(
                        parent,
                        customView).setDuration(duretion)
            }catch ( e: Exception){
                Log.v("exception ",e.message)
            }

            return null
        }

    }

}