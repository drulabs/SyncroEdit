package org.drulabs.syncroedit.services;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.utils.PrefsHelper;

/**
 * Authored by KaushalD on 10/10/2016.
 */

public class FCMTokenFetcher extends FirebaseInstanceIdService {

    private static final String TAG = "FCMTokenFetcher";

    @Override
    public void onTokenRefresh() {
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        saveToken(fcmToken);
    }

    private void saveToken(String fcmToken) {

        Log.d(TAG, "FCMTokenFetcher: token: " + fcmToken);

        PrefsHelper prefsHelper = PrefsHelper.getInstance(this);
        prefsHelper.saveFCMToken(fcmToken);

        if (prefsHelper.getUserUID() != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants
                    .USER_BASE).child(prefsHelper.getUserUID()).child(Constants.KEY_FCM_TOKEN)
                    .setValue(fcmToken);
        }
    }
}
