/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.omnirom.device;

import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.view.MenuItem;

import android.preference.MultiSelectListPreference;

import android.preference.ListPreference;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.Set;

public class DeviceSettings extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = DeviceSettings.class.getSimpleName();

    public static final String KEY_DOUBLE_TAP_SWITCH = "double_tap";
    public static final String KEY_CAMERA_SWITCH = "camera";
    public static final String KEY_TORCH_SWITCH = "torch";
    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_OCLICK_CATEGORY = "oclick_category";
    public static final String KEY_OCLICK = "oclick";

    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";
    private static final String KEY_CAMERA_LAUNCH_INTENT =
        "touchscreen_gesture_camera_launch_intent";
    private static final String KEY_TORCH_LAUNCH_INTENT = "touchscreen_gesture_torch_launch_intent";

    private static final String KEY_CAMERA_FEEDBACK = "touchscreen_gesture_camera_feedback";
    private static final String KEY_TORCH_FEEDBACK  = "touchscreen_gesture_torch_feedback";

    //private TwoStatePreference mDoubleTapSwitch;
    private TwoStatePreference mTorchSwitch;
    private TwoStatePreference mCameraSwitch;
    private VibratorStrengthPreference mVibratorStrength;
    private Preference mOClickPreference;

    private MultiSelectListPreference mHapticFeedback;
    private ListPreference mCameraLaunchIntent;
    private ListPreference mTorchLaunchIntent;

    private String preferenceKeyLastChangedShortcut;

    private static final int REQUEST_PICK_SHORTCUT = 100;
    private static final int REQUEST_CREATE_SHORTCUT = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.main);

        /*mDoubleTapSwitch = (TwoStatePreference) findPreference(KEY_DOUBLE_TAP_SWITCH);
        mDoubleTapSwitch.setEnabled(DoubleTapSwitch.isSupported());
        mDoubleTapSwitch.setChecked(DoubleTapSwitch.isEnabled(this));
        mDoubleTapSwitch.setOnPreferenceChangeListener(new DoubleTapSwitch());*/

        mTorchSwitch = (TwoStatePreference) findPreference(KEY_TORCH_SWITCH);
        mTorchSwitch.setEnabled(TorchGestureSwitch.isSupported());
        mTorchSwitch.setChecked(TorchGestureSwitch.isEnabled(this));
        mTorchSwitch.setOnPreferenceChangeListener(new TorchGestureSwitch());

        mCameraSwitch = (TwoStatePreference) findPreference(KEY_CAMERA_SWITCH);
        mCameraSwitch.setEnabled(CameraGestureSwitch.isSupported());
        mCameraSwitch.setChecked(CameraGestureSwitch.isEnabled(this));
        mCameraSwitch.setOnPreferenceChangeListener(new CameraGestureSwitch());

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());

        final boolean oclickEnabled = getResources().getBoolean(R.bool.config_has_oclick);
        PreferenceCategory oclickCategory = (PreferenceCategory) findPreference(KEY_OCLICK_CATEGORY);
        if (!oclickEnabled) {
            getPreferenceScreen().removePreference(oclickCategory);
        }
        mOClickPreference = (Preference) findPreference(KEY_OCLICK);

        mHapticFeedback = (MultiSelectListPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        mHapticFeedback.setOnPreferenceChangeListener(this);

        mCameraLaunchIntent = (ListPreference) findPreference(KEY_CAMERA_LAUNCH_INTENT);
        mCameraLaunchIntent.setOnPreferenceChangeListener(this);

        mTorchLaunchIntent = (ListPreference) findPreference(KEY_TORCH_LAUNCH_INTENT);
        mTorchLaunchIntent.setOnPreferenceChangeListener(this);

        new InitListTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mOClickPreference) {
            Intent i = new Intent(Intent.ACTION_MAIN).setClassName("org.omnirom.omniclick","org.omnirom.omniclick.OClickControlActivity");
            startActivity(i);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        if (KEY_HAPTIC_FEEDBACK.equals(key)) {
            final Set<String> value = (Set<String>) newValue;
            final CharSequence[] valueOptions = mHapticFeedback.getEntryValues();
            if(!value.isEmpty()){
                for (CharSequence valueOption : valueOptions) {
                    if (value.contains(valueOption.toString())) {
                        Settings.System.putInt(getContentResolver(), valueOption.toString(), 1);
                        Log.d(TAG, "Put int: " + 1 + " for key: " + valueOption.toString());
                    } else {
                        Settings.System.putInt(getContentResolver(), valueOption.toString(), 0);
                        Log.d(TAG, "Put int: " + 0 + " for key: " + valueOption.toString());
                    }
                }
            } else{
                Log.d(TAG, "Put int: " + 0 + " for both");
                Settings.System.putInt(getContentResolver(), KEY_CAMERA_FEEDBACK, 0);
                Settings.System.putInt(getContentResolver(), KEY_TORCH_FEEDBACK, 0);
            }
            return true;
        }
        if(KEY_CAMERA_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_CAMERA_LAUNCH_INTENT);
            } else{
                Settings.System.putString(getContentResolver(), KEY_CAMERA_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        if(KEY_TORCH_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_TORCH_LAUNCH_INTENT);
            } else{
                Settings.System.putString(getContentResolver(), KEY_TORCH_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_SHORTCUT) {
                startActivityForResult(data, REQUEST_CREATE_SHORTCUT);
            }
            if(requestCode == REQUEST_CREATE_SHORTCUT){
                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, data.getStringExtra(
                        Intent.EXTRA_SHORTCUT_NAME));
                String uri = intent.toUri(Intent.URI_INTENT_SCHEME);
                if(preferenceKeyLastChangedShortcut != null){
                    Settings.System.putString(getContentResolver(),
							preferenceKeyLastChangedShortcut, uri);
                    reloadSummarys();
                }
            }
        } else{
            Settings.System.putString(getContentResolver(),
                    preferenceKeyLastChangedShortcut, "default");
            reloadSummarys();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createShortcutPicked(String key){
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
        preferenceKeyLastChangedShortcut = key;
    }

    private List<String> getPackageNames(){
        List<String> packageNameList = new ArrayList<String>();
        List<PackageInfo> packs =
				getApplicationContext().getPackageManager().getInstalledPackages(0);
        for(int i = 0; i < packs.size(); i++){
            String packageName = packs.get(i).packageName;
            Intent launchIntent = getApplicationContext().getPackageManager()
            .getLaunchIntentForPackage(packageName);
            if(launchIntent != null){
                packageNameList.add(packageName);
            }
        }
        return packageNameList;
    }

    private String getAppnameFromPackagename(String packagename){
        if(packagename == null || "".equals(packagename)){
            return getResources().getString(R.string.touchscreen_action_default);
        }
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packagename, 0);
        } catch (final Exception e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packagename);
    }

    private String getSummary(String key){
        String summary = Settings.System.getString(getContentResolver(), key);
        if(summary == null){
            return getResources().getString(R.string.touchscreen_action_unkownappforpackagename);
        }
        else if(summary.startsWith("intent:")){
            return getResources().getString(R.string.touchscreen_action_shortcut);
        }
        else if(summary.equals("default")){
            return getResources().getString(R.string.touchscreen_action_default);
        }
        return getAppnameFromPackagename(summary);
    }

    private void reloadSummarys(){
        mCameraLaunchIntent.setSummary(getSummary(KEY_CAMERA_LAUNCH_INTENT));
        mTorchLaunchIntent.setSummary(getSummary(KEY_TORCH_LAUNCH_INTENT));
    }

    private class InitListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            List<String> listPackageNames = getPackageNames();
            listPackageNames.add(0, "default");
            listPackageNames.add(1, "shortcut");
            final CharSequence[] packageNames =
                    listPackageNames.toArray(new CharSequence[listPackageNames.size()]);
            final CharSequence[] hrblPackageNames = new CharSequence[listPackageNames.size()];

            for(int i = 0; i < listPackageNames.size(); i++){
                hrblPackageNames[i] = getAppnameFromPackagename(listPackageNames.get(i));
            }

			hrblPackageNames[0] = getResources().getString(R.string.touchscreen_action_default);
            hrblPackageNames[1] = getResources().getString(R.string.touchscreen_action_shortcut);

            mCameraLaunchIntent.setEntries(hrblPackageNames);
            mCameraLaunchIntent.setEntryValues(packageNames);

            mTorchLaunchIntent.setEntries(hrblPackageNames);
            mTorchLaunchIntent.setEntryValues(packageNames);

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            reloadSummarys();
            mCameraLaunchIntent.setEnabled(true);
            mTorchLaunchIntent.setEnabled(true);
        }
    }
}
