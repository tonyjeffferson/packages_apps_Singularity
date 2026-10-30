/*
 * Copyright (C) 2026 Lunaris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lunaris.settings.fragments.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

final class UnifiedStatusPreferenceController {

    static final String PREFERENCE_KEY = "unified_status_indicator";
    static final String BATTERY_STYLE_KEY = "status_bar_battery_style";

    private final ContentResolver mResolver;
    private final Preference mUnifiedStatusPreference;
    private final Preference mBatteryStylePreference;
    private boolean mObserverRegistered;

    private final ContentObserver mObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            updateBatteryStyleEnabled();
        }
    };

    UnifiedStatusPreferenceController(Context context, Preference unifiedStatusPreference,
            Preference batteryStylePreference) {
        mResolver = context.getContentResolver();
        mUnifiedStatusPreference = unifiedStatusPreference;
        mBatteryStylePreference = batteryStylePreference;

        if (mUnifiedStatusPreference != null) {
            mUnifiedStatusPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (mBatteryStylePreference != null) {
                    mBatteryStylePreference.setEnabled(!Boolean.TRUE.equals(newValue));
                }
                return true;
            });
        }
        updateBatteryStyleEnabled();
    }

    void onResume() {
        if (mUnifiedStatusPreference == null || mObserverRegistered) {
            return;
        }
        mResolver.registerContentObserver(Settings.Secure.getUriFor(PREFERENCE_KEY), false,
                mObserver, UserHandle.USER_ALL);
        mObserverRegistered = true;
        updateBatteryStyleEnabled();
    }

    void onPause() {
        if (!mObserverRegistered) {
            return;
        }
        mResolver.unregisterContentObserver(mObserver);
        mObserverRegistered = false;
    }

    private void updateBatteryStyleEnabled() {
        if (mBatteryStylePreference != null) {
            boolean enabled = Settings.Secure.getIntForUser(mResolver, PREFERENCE_KEY, 0,
                    UserHandle.USER_CURRENT) == 0;
            mBatteryStylePreference.setEnabled(enabled);
        }
    }
}
