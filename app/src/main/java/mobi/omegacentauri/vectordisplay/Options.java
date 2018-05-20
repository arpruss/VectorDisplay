package mobi.omegacentauri.vectordisplay;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Options extends PreferenceActivity {
    public static final String PREF_LANDSCAPE = "landscape";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.options);
//
//		Preference p = getPreferenceScreen().findPreference(PREF_VOLUME);
//		if (p != null) {
//			SpeakerBoost.log("Setting PREF_VOLUME to "+defaultShowVolume());
//			p.setDefaultValue(defaultShowVolume());
//		}
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}