package pl.farmaprom;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Preferences {
    SharedPreferences preferences;

    private final String PREFERENCE_PATH = "path";

    static final String DEFAULT_PREFERENCE_STRING = "";


    public Preferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isPathSet() {
        return (!TextUtils.equals(getStringPreference(PREFERENCE_PATH), DEFAULT_PREFERENCE_STRING));
    }

    public String getPath() {
        return getStringPreference(PREFERENCE_PATH);
    }

    public void setPath(String value) {
        setStringPreference(PREFERENCE_PATH, value);
    }

    private String getStringPreference(String key) {
        return preferences.getString(key, DEFAULT_PREFERENCE_STRING);
    }

    private void setStringPreference(String key, String value) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(key, value);
        preferencesEditor.commit();
    }

    private void removePreference(String key) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.remove(key);
        preferencesEditor.commit();
    }

}

