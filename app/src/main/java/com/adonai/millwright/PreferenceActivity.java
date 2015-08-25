package com.adonai.millwright;

import android.os.Bundle;

/**
 * Preference activity responsible for user settings handling
 * For now just loads single preference fragment
 *
 * @author Adonai
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {

    PreferenceMainFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        mFragment = (PreferenceMainFragment) getFragmentManager().findFragmentById(R.id.preference_fragment);
    }
}
