package org.drulabs.syncroedit.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Authored by KaushalD on 9/27/2016.
 * <p>
 * As shared preferences read/write is costly operation, we are using this singleton class to
 * deal with preferences. This saves the data locally and if data is present in local variables,
 * that is uses instead of re-reading from preferences. This is a little bit memory overhead, but
 * has a good performance.
 */

public class PrefsHelper {

    private static final String PREFS_NAME = "holy_crabby";
    private Context mContext;
    private static PrefsHelper prefsHelper = null;

    private SharedPreferences prefs = null;
    private SharedPreferences.Editor prefsEditor = null;

    private PrefsHelper(Context nContext) {
        this.mContext = nContext;
        prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    public static PrefsHelper getInstance(Context nContext) {
        if (prefsHelper == null) {
            prefsHelper = new PrefsHelper(nContext);
        }
        return prefsHelper;
    }

    private static final String KEY_MY_NAME = "user_name";
    private String myName = null;

    private static final String KEY_MY_PIC_URL = "user_pic_url";
    private String myPicUrl = null;

    private static final String KEY_MY_EMAIL = "user_email";
    private String myEmail = null;

    private static final String KEY_MY_UID = "user_uid";
    private String myUID = null;

    private static final String KEY_MY_PHONE = "user_phone";
    private String myPhone = null;

    private static final String KEY_OTP = "verification_otp";
    private String receivedOtp = null;

    private static final String KEY_FCM_TOKEN = "fcm_token";
    private String fcmToken = null;

    public void saveUserName(String userName) {
        this.myName = userName;
        prefsEditor.putString(KEY_MY_NAME, userName);
        prefsEditor.apply();
    }

    public String getUserName() {
        if (myName == null) {
            myName = prefs.getString(KEY_MY_NAME, null);
        }
        return myName;
    }

    public void saveUserPic(String picUrl) {
        this.myPicUrl = picUrl;
        prefsEditor.putString(KEY_MY_PIC_URL, picUrl);
        prefsEditor.apply();
    }

    public String getUserPic() {
        if (myPicUrl == null) {
            myPicUrl = prefs.getString(KEY_MY_PIC_URL, null);
        }
        return myPicUrl;
    }

    public void saveUserEmail(String email) {
        this.myEmail = email;
        prefsEditor.putString(KEY_MY_EMAIL, myEmail);
        prefsEditor.apply();
    }

    public String getUserEmail() {
        if (myEmail == null) {
            myEmail = prefs.getString(KEY_MY_EMAIL, null);
        }
        return myEmail;
    }

    public void saveUserUID(String uid) {
        this.myUID = uid;
        prefsEditor.putString(KEY_MY_UID, uid);
        prefsEditor.apply();
    }

    public String getUserUID() {
        if (myUID == null) {
            myUID = prefs.getString(KEY_MY_UID, null);
        }
        return myUID;
    }

    public void saveUserPhone(String phone) {
        this.myPhone = phone;
        prefsEditor.putString(KEY_MY_PHONE, myPhone);
        prefsEditor.apply();
    }

    public String getUserPhone() {
        if (myPhone == null) {
            myPhone = prefs.getString(KEY_MY_PHONE, null);
        }
        return myPhone;
    }

    public void saveOTP(String otp) {
        this.receivedOtp = otp;
        prefsEditor.putString(KEY_OTP, receivedOtp);
        prefsEditor.apply();
    }

    public String getOTP() {
        if (receivedOtp == null) {
            receivedOtp = prefs.getString(KEY_OTP, null);
        }
        return receivedOtp;
    }

    public void saveFCMToken(String token) {
        this.fcmToken = token;
        prefsEditor.putString(KEY_FCM_TOKEN, token);
        prefsEditor.apply();
    }

    public String getFCMToken() {
        if (fcmToken == null) {
            fcmToken = prefs.getString(KEY_FCM_TOKEN, null);
        }
        return fcmToken;
    }
}
