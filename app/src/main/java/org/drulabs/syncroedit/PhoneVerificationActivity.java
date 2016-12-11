package org.drulabs.syncroedit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.PrefsHelper;

import static org.drulabs.syncroedit.ContactListActivity.MAX_PHONE_LENGTH;

public class PhoneVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etPhone, etOtp;
    Button btnVerify;

    private PrefsHelper prefsHelper;

    private static final int WRITE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        setToolBarTitle(R.string.phone_verification_title);

        prefsHelper = PrefsHelper.getInstance(PhoneVerificationActivity.this);

        if (prefsHelper.getUserPhone() != null) {
            openNotesActivity();
        }

        etPhone = (EditText) findViewById(R.id.et_phone_number);
        etOtp = (EditText) findViewById(R.id.et_phone_otp);
        btnVerify = (Button) findViewById(R.id.btn_verify_phone);
        btnVerify.setOnClickListener(this);

        //checkSDCardPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        switch (requestCode) {
            case WRITE_PERMISSION_CODE:
                if (writePermission.equalsIgnoreCase(permissions[0]) && grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {
                    NotificationToast.showToast(this, "This permission is required to continue");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_verify_phone:
                //TODO write phone verification logic here. for now just moving to next activity

                String phone = etPhone.getText().toString();
                String otp = etOtp.getText().toString();

                if (phone != null && phone.trim().length() > 0 && otp != null && otp.trim()
                        .length() > 0) {

                    phone = phone.replaceAll("\\D+", "");

                    int subStringStart = 0;

                    int contactNumLen = phone.length();

                    if (contactNumLen > MAX_PHONE_LENGTH) {
                        subStringStart = contactNumLen - MAX_PHONE_LENGTH;
                    }

                    phone = phone.substring(subStringStart);

                    prefsHelper.saveUserPhone(phone);
                    prefsHelper.saveOTP(otp);

                    openNotesActivity();

                }
                break;
            default:
                break;
        }
    }

//    private void checkSDCardPermission() {
//        String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        boolean isGranted = Utility.checkPermission(writePermission, this);
//        if (!isGranted) {
//            Utility.requestPermission(writePermission, WRITE_PERMISSION_CODE, this);
//        }
//    }

    private void setToolBarTitle(int resId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(resId));
        }
    }

    private void openNotesActivity() {
        Intent notesIntent = new Intent(PhoneVerificationActivity.this, NotesActivity
                .class);
        startActivity(notesIntent);
        finish();
    }
}
