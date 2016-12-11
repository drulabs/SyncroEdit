package org.drulabs.syncroedit.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.drulabs.syncroedit.NoteEditorActivity;
import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.model.Note;

import java.util.Map;

/**
 * Authored by KaushalD on 10/10/2016.
 */

public class FCMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Map<String, String> receivedMap = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + receivedMap);

            handleFCM(receivedMap);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void handleFCM(Map<String, String> receivedMap) {
        FirebaseCrash.report(new Throwable("One weird exception: FCM received"));
        int fcmType = Integer.parseInt(receivedMap.get(Constants.KEY_FCM_TYPE));
        switch (fcmType) {
            case Constants.TYPE_NOTE_COLLAB:

                String requesterName = receivedMap.get(Constants.KEY_USER_NAME);
                String noteTitle = receivedMap.get(Constants.KEY_NOTE_TITLE);
                String noteId = receivedMap.get(Constants.KEY_NOTE_ID);

                final String content = getString(R.string.note_collab_notif_content, requesterName,
                        noteTitle);

                final String notificationTitle = getString(R.string.note_collab_notif_title);

                final Intent notifActionIntent = new Intent(this, NoteEditorActivity.class);

                FirebaseDatabase.getInstance().getReference().child
                        (Constants.NOTES_DATABASE).child(noteId).addListenerForSingleValueEvent
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Note note = dataSnapshot.getValue(Note.class);
                                notifActionIntent.putExtra(NoteEditorActivity.KEY_NOTE_ITEM, note);

                                showNotification(notificationTitle, content, notifActionIntent);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                break;
            default:
                break;
        }
    }

    private void showNotification(String title, String content, Intent actionIntent) {

        FirebaseCrash.log("Notification created from data");

        PendingIntent pendingActionIntent = PendingIntent.getActivity(this, 0, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon
                (R.mipmap.ic_list_img).setContentTitle(title).setContentText(content)
                .setContentIntent(pendingActionIntent).setAutoCancel(true);
        NotificationManager notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifMgr.notify((int) (System.currentTimeMillis() / 1000), mBuilder.build());
    }

}
