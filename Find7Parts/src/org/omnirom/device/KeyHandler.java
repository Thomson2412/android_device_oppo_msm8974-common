/*
* Copyright (C) 2015 The OmniROM Project
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.os.DeviceKeyHandler;
import com.android.internal.util.ArrayUtils;

import android.os.Vibrator;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import java.net.URISyntaxException;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = KeyHandler.class.getSimpleName();
    private static final boolean DEBUG = false;
    protected static final int GESTURE_REQUEST = 1;
    private static final int GESTURE_WAKELOCK_DURATION = 3000;

    // Supported scancodes
    private static final int GESTURE_CIRCLE_SCANCODE = 62;
    private static final int GESTURE_V_SCANCODE = 63;
    private static final int KEY_DOUBLE_TAP = 61;

    private static final String BUTTON_DISABLE_FILE = "/sys/kernel/touchscreen/button_disable";

    private static final int[] sSupportedGestures = new int[]{
        GESTURE_CIRCLE_SCANCODE,
        GESTURE_V_SCANCODE,
        KEY_DOUBLE_TAP
    };

    private static final int[] sHandledGestures = new int[]{
        GESTURE_V_SCANCODE
    };

    private static final String KEY_CAMERA_LAUNCH_INTENT = "touchscreen_gesture_camera_launch_intent";
    private static final String KEY_TORCH_LAUNCH_INTENT = "touchscreen_gesture_torch_launch_intent";

    private static final String KEY_CAMERA_FEEDBACK = "touchscreen_gesture_camera_feedback";
    private static final String KEY_TORCH_FEEDBACK  = "touchscreen_gesture_torch_feedback";


    private static final String ACTION_DISMISS_KEYGUARD =
            "com.android.keyguard.action.DISMISS_KEYGUARD_SECURELY";

    protected final Context mContext;
    private final PowerManager mPowerManager;
    private EventHandler mEventHandler;
    private WakeLock mGestureWakeLock;
    private Handler mHandler = new Handler();
    private SettingsObserver mSettingsObserver;
    private Vibrator mVibrator;

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.HARDWARE_KEYS_DISABLE),
                    false, this);
            update();
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        public void update() {
            setButtonDisable(mContext);
        }
    }

    public KeyHandler(Context context) {
        mContext = context;
        mEventHandler = new EventHandler();
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mGestureWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GestureWakeLock");
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            mVibrator = null;
        }
    }

    private class EventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            KeyEvent event = (KeyEvent) msg.obj;
            switch(event.getScanCode()) {
            case GESTURE_V_SCANCODE:
                Log.d(TAG, "Case == GESTURE_V_SCANCODE: " + event.getScanCode());
                if(!launchIntentFromKey(KEY_TORCH_LAUNCH_INTENT)) {
                    Log.d(TAG, "Toggel flashlight");
                    if (DEBUG) Log.i(TAG, "GESTURE_V_SCANCODE");
                    mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                    Intent torchIntent = new Intent("com.android.systemui.TOGGLE_FLASHLIGHT");
                    torchIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
                    mContext.sendBroadcastAsUser(torchIntent, user);
                }
                else{
                    Log.d(TAG, "Launch action");
                }
                doHapticFeedback(KEY_TORCH_FEEDBACK);
                break;
            }
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        boolean isKeySupported = ArrayUtils.contains(sHandledGestures, event.getScanCode());
        if (isKeySupported && !mEventHandler.hasMessages(GESTURE_REQUEST)) {
            if (DEBUG) Log.i(TAG, "scanCode=" + event.getScanCode());
            Message msg = getMessageForKeyEvent(event);
            mEventHandler.sendMessage(msg);
        }
        return isKeySupported;
    }

    @Override
    public boolean canHandleKeyEvent(KeyEvent event) {
        return ArrayUtils.contains(sSupportedGestures, event.getScanCode());
    }

    @Override
    public boolean isDisabledKeyEvent(KeyEvent event) {
        return false;
    }

    private Message getMessageForKeyEvent(KeyEvent keyEvent) {
        Message msg = mEventHandler.obtainMessage(GESTURE_REQUEST);
        msg.obj = keyEvent;
        return msg;
    }

    public static void setButtonDisable(Context context) {
        final boolean disableButtons = Settings.System.getInt(
                context.getContentResolver(), Settings.System.HARDWARE_KEYS_DISABLE, 0) == 1;
        if (DEBUG) Log.i(TAG, "setButtonDisable=" + disableButtons);
        Utils.writeValue(BUTTON_DISABLE_FILE, disableButtons ? "1" : "0");
    }

    @Override
    public boolean isCameraLaunchEvent(KeyEvent event) {
        Log.d(TAG, "isCameraLaunchEvent code: " + event.getScanCode());
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        if(event.getScanCode() == GESTURE_CIRCLE_SCANCODE) {
            Log.d(TAG, "Code == GESTURE_CIRCLE_SCANCODE: " + event.getScanCode());
            if (launchIntentFromKey(KEY_CAMERA_LAUNCH_INTENT)) {
                Log.d(TAG, "Luanch action");
                doHapticFeedback(KEY_CAMERA_FEEDBACK);
                return false;
            } else {
                Log.d(TAG, "Launch camera");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWakeEvent(KeyEvent event){
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        return event.getScanCode() == KEY_DOUBLE_TAP;
    }

    @Override
    public KeyEvent translateKeyEvent(KeyEvent event) {
        return null;
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
            mContext.startActivityAsUser(intent, null, user);
        } catch (ActivityNotFoundException e) {
            // Ignore
        }
    }

    private void doHapticFeedback(String key) {
        Log.d(TAG, "Check haptic feedback for: " + key);
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(), key, 0) != 0;
        if(enabled){
            Log.d(TAG, "Do haptic feedback for (enabled): " + key);
            doHapticFeedback();
        }
    }

    private void doHapticFeedback() {
        Log.d(TAG, "Do haptic feedback");
        if (mVibrator == null) {
            Log.d(TAG, "Vibrator == null");
            return;
        }
        mVibrator.vibrate(200);
    }

    private boolean launchIntentFromKey(String key){
        String packageName = Settings.System.getString(mContext.getContentResolver(), key);
        if(packageName == null){
            return false;
        }
        Intent intent = null;
        if(packageName.equals("") || packageName.equals("default")){
            return false;
        }
        else if(packageName.startsWith("intent:")){
            Log.d("KeyHandler", "packageName.equals(shortcut)");
            try{
                Log.d("KeyHandler", "Try shortcut");
                intent = Intent.parseUri(packageName, Intent.URI_INTENT_SCHEME);
            } catch(URISyntaxException e){
                Log.d("KeyHandler", "Shortcut failed");
                e.printStackTrace();
                return false;
            }
        }
        else{
            Log.d("KeyHandler", "NOT packageName.equals(shortcut)");
            intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        }
        if(intent != null){
            mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
            mContext.sendBroadcastAsUser(new Intent(ACTION_DISMISS_KEYGUARD), UserHandle.CURRENT);
            startActivitySafely(intent);
            return true;
        }
        return false;
    }
}
