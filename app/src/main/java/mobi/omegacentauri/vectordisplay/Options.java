package mobi.omegacentauri.vectordisplay;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Options extends PreferenceActivity {
    public static final String PREF_LANDSCAPE = "landscape";
    public static final String PREF_RESET_ON_CONNECT = "resetOnConnect";
    public static final String PREF_UPDATE_SPEED = "speed";

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

        setRequestedOrientation(
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Options.PREF_LANDSCAPE, true) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}