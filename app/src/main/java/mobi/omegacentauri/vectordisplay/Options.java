package mobi.omegacentauri.vectordisplay;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Options extends PreferenceActivity {
    public static final String PREF_LANDSCAPE = "landscape";
    public static final String PREF_RESET_ON_CONNECT = "resetOnConnect";
    public static final String PREF_FPS = "fps";
    public static final String PREF_CONNECTION = "connection";
    public static final int OPT_USB = 0;
    public static final int OPT_IP = 1;
    public static final int OPT_BLUETOOTH = 2;
    public static final int NUM_CONNECTION_OPTS = 3;
    public static final String PREF_LAST_BLUETOOTH_ADDRESS = "lastBT";

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
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Options.PREF_LANDSCAPE, false) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

}