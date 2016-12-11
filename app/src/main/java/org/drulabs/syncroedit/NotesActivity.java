package org.drulabs.syncroedit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.drulabs.syncroedit.fragments.NotesFragment;
import org.drulabs.syncroedit.model.Note;
import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.PrefsHelper;

public class NotesActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, NotesFragment.OnListFragmentInteractionListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "NotesActivity";

    private String mUsername;
    private String mPhotoUrl;

    private PrefsHelper prefsHelper;

    private GoogleApiClient mGoogleApiClient;

    private NotesFragment notesFragment = null;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefsHelper = PrefsHelper.getInstance(NotesActivity.this);

        mUsername = prefsHelper.getUserName();
        mPhotoUrl = prefsHelper.getUserPic();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        findViewById(R.id.fab_new_note).setOnClickListener(this);

        loadNotesListFragment();
        setToolBarTitle(getString(R.string.my_notes_text));
    }

    private void loadNotesListFragment() {
        notesFragment = NotesFragment.newInstance();
        notesFragment.setRetainInstance(true);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.notes_fragment_holder, notesFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_notes_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                signOut();
                return true;
            case R.id.add_new_note:
                Intent noteEditorIntent = new Intent(NotesActivity.this, NoteEditorActivity.class);
                startActivity(noteEditorIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_new_note:
                Intent noteEditorIntent = new Intent(NotesActivity.this, NoteEditorActivity.class);
                startActivity(noteEditorIntent);
                break;
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mUsername = ANONYMOUS;
        startActivity(new Intent(this, SignInActivity.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        NotificationToast.showToast(this, "Google Play Services error.");
    }

    @Override
    public void onListFragmentInteraction(Note note) {
        Intent noteEditorIntent = new Intent(NotesActivity.this, NoteEditorActivity.class);
        noteEditorIntent.putExtra(NoteEditorActivity.KEY_NOTE_ITEM, note);
        startActivity(noteEditorIntent);
    }

    private void setToolBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}