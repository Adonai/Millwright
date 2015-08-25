package com.adonai.millwright;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.CompoundButton;

/**
 * Dialog fragment showing preferences editing form
 *
 * @author adonai
 */
public class PreferenceMainFragment extends PreferenceFragment {

    public static final String ASK_FOR_DELETE = "ask.for.delete";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}
