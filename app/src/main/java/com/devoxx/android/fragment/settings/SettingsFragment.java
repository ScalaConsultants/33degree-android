package com.devoxx.android.fragment.settings;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;

import com.devoxx.R;
import com.devoxx.android.activity.SelectorActivity_;
import com.devoxx.data.Settings;
import com.devoxx.data.Settings_;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.user.UserManager;
import com.devoxx.utils.InfoUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceClick;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EFragment
public class SettingsFragment extends PreferenceFragment {

    @Bean
    ConferenceManager conferenceManager;

    @Bean
    UserManager userManager;

    @Bean
    InfoUtil infoUtil;

    @Pref
    Settings_ settings;

    @AfterViews
    void afterViews() {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserCodePreference();
    }

    @PreferenceClick(R.string.settings_user_code)
    void onClearUserCodeClick() {
        if (userManager.isFirstTimeUser()) {
            userManager.openUserScanBadge();
        } else {
            userManager.clearCode();
            infoUtil.showToast("Code has been cleared!");
        }
        setupUserCodePreference();
    }

    @PreferenceClick(R.string.settings_change_conf_key)
    void onChangeConferenceClick() {
        ActivityCompat.finishAffinity(getActivity());

        conferenceManager.requestConferenceChange();

        SelectorActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    private void setupUserCodePreference() {
        final Preference preference = getPreferenceManager().findPreference(
                getString(R.string.settings_user_code));
        final String title;
        final String summary;
        if (userManager.isFirstTimeUser()) {
            title = "Scan QR badge";
            summary = "Open scanner and scans users QR code from the badge.";
        } else {
            title = "Clear QR code";
            summary = "Clears previously scanned QR code.";
        }
        preference.setTitle(title);
        preference.setSummary(summary);
    }
}
