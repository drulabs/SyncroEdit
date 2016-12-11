package org.drulabs.syncroedit;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.notification.NotificationToast;

public class NoteImageActivity extends AppCompatActivity {

    ImageView noteImage = null;

    public static final String KEY_NOTE_ID = "note_id";

    StorageReference storageReference;

    ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_image);

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(KEY_NOTE_ID)) {
            NotificationToast.showToast(NoteImageActivity.this, "Invalid argument passed");
            finish();
            return;
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading_note_image));

        String noteId = extras.getString(KEY_NOTE_ID);

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://" + Constants
                .NOTES_IMAGE_BUCKET);
        storageReference = storageReference.child(Constants.NOTES_IMAGE_FOLDER).child(noteId + "" +
                ".jpg");

        noteImage = (ImageView) findViewById(R.id.imageView_note);

        dialog.show();
        Glide.with(this).using(new FirebaseImageLoader()).load(storageReference).listener(
                new
                        RequestListener<StorageReference, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, StorageReference model,
                                                       Target<GlideDrawable> target, boolean isFirstResource) {
                                dialog.dismiss();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource,
                                                           StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                dialog.dismiss();
                                return false;
                            }
                        }).into
                (noteImage);
    }
}
