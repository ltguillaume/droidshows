package org.droidseries.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

//TODO: implement this

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnClickListener {
        // Current settings handler
        private static SharedPreferences preferences;

        // Context definition
        private Context context = null;

        private final static String profilePath = "/sdcard/DroidSeries/";

        private final String sharedPrefsPath = "/data/data/org.droidseries/shared_prefs/";

        private final String sharedPrefsFile = "org.droidseries_preferences";

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                        String key) {
                // TODO Auto-generated method stub

        }

        public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub

        }

}
