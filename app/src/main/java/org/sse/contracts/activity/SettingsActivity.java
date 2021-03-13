package org.sse.contracts.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import org.sse.contracts.Constants;
import org.sse.contracts.R;
import org.sse.contracts.core.conf.UserConfigurations;

import java.text.SimpleDateFormat;

import trikita.log.Log;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();
            Log.d("onPreferenceChange, key: " + preference.getKey() + ", value: " + stringValue);

            if (preference instanceof ListPreference)
            {
                ListPreference listPreference = (ListPreference) preference;

                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else if (preference instanceof EditTextPreference)
            {
                EditTextPreference pref = (EditTextPreference) preference;

                if (UserConfigurations.KEY_DATE_FORMAT.equals(pref.getKey()) )
                {
                    if (stringValue == null || stringValue.isEmpty())
                    {
                        stringValue = Constants.DEFAULT_PDF_DATE_FORMAT;
                        pref.setText(stringValue);
                        pref.setSummary(stringValue);
                        return false;
                    }

                    if (!checkDateFormat(stringValue))
                    {
                        return false;
                    }
                }

                preference.setSummary(stringValue);
            }
            else
            {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private boolean checkDateFormat(String value)
        {
            if (value == null || value.isEmpty()) return false;
            try
            {
                new SimpleDateFormat(value);
            }
            catch(Exception ex)
            {
                return false;
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(PreferenceFragment owner, String prefKey)
    {
        Preference preference = owner.findPreference(prefKey);
        if (preference == null)
        {
            Log.e("Not found preference for: " + prefKey);
            return;
        }

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupActionBar();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_DATE_FORMAT);
            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_FONT_SIZE);
            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_MARGIN_LEFT);
            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_MARGIN_RIGHT);
            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_MARGIN_TOP);
            bindPreferenceSummaryToValue(this, UserConfigurations.KEY_MARGIN_BOTTOM);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home)
            {
                Activity activity = getActivity();
                if (activity != null)
                {
                    activity.onBackPressed();
                    return true;
                }
                Log.e("Activity not found!");
                return false;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
