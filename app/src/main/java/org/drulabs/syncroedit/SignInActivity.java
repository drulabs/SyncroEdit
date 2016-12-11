package org.drulabs.syncroedit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.model.ConnectedServiceInfo;
import org.drulabs.syncroedit.model.User;
import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.PrefsHelper;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient
        .OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignIn";
    private static final int GOOGLE_SIGN_IN = 9001;

    private PrefsHelper prefsHelper;

    private SignInButton mGoogleSignIn;
    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog dialog;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFirebaseDBReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Assign fields
        mGoogleSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        // Set click listeners
        mGoogleSignIn.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestScopes(new Scope(Scopes.PROFILE))
//                .requestScopes(new Scope(Scopes.PLUS_ME))
//                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDBReference = FirebaseDatabase.getInstance().getReference().child(Constants
                .USER_BASE);

        // Others
        prefsHelper = PrefsHelper.getInstance(SignInActivity.this);
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                dialog.show();
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            saveGoogleUser(acct);
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            startActivity(new Intent(SignInActivity.this, NotesActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void saveGoogleUser(GoogleSignInAccount acct) {

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();
        String picURL = null;
        if (firebaseUser != null && firebaseUser.getPhotoUrl() != null) {
            picURL = firebaseUser.getPhotoUrl().toString();
        }
        String userName = firebaseUser.getDisplayName();
        String userEmail = firebaseUser.getEmail();
        String providerId = firebaseUser.getProviderId();

        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        User currentUser = new User();
        currentUser.setName(userName);
        currentUser.setEmail(userEmail);
        currentUser.setServiceProviderId(providerId);
        currentUser.setPhotoUrl(picURL);
        currentUser.setSignInServiceType(User.GOOGLE);
        currentUser.setUserId(userUID);
        currentUser.setPhone(prefsHelper.getUserPhone());
        currentUser.setFcmToken(fcmToken);

        ConnectedServiceInfo serviceInfo = new ConnectedServiceInfo();
        serviceInfo.setServiceName(User.GOOGLE);
        serviceInfo.setConnected(true);
        serviceInfo.setAccessToken(acct.getIdToken());
        serviceInfo.setAccessSecret(acct.getId());
        serviceInfo.setSecretKey(acct.getServerAuthCode());

        currentUser.addService(serviceInfo);

        mFirebaseDBReference.child(userUID).updateChildren(User.getMapRep(currentUser));
        mFirebaseDBReference.child(userUID).child(Constants.USER_BASE_SERVICES).setValue
                (currentUser.getServices());


        // Save user info locally
        prefsHelper.saveUserEmail(userEmail);
        prefsHelper.saveUserName(userName);
        prefsHelper.saveUserPic(picURL);
        prefsHelper.saveUserUID(userUID);
        prefsHelper.saveFCMToken(fcmToken);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        NotificationToast.showToast(this, "Google Play Services error.");
    }
}
