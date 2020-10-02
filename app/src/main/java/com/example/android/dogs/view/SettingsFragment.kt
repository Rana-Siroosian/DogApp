package com.example.android.dogs.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.example.android.dogs.R
/**
 * author: RanaSiroosian
 */
//We have to make sure that we include this fragment in our navigation
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /**The preferences library will manage everything for us and it will create the whole
         * screen based on this line of code below
         */
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }


}