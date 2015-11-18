package com.davidissamattos.clockcalendar;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by David on 12/11/15.
 */
public class SettingsFragment extends PreferenceFragment
{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_fragment);
    }
}
