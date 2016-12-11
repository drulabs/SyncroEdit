package org.drulabs.syncroedit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.drulabs.syncroedit.adapters.NoteItemAdapter;
import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.model.Note;
import org.drulabs.syncroedit.model.NoteItem;
import org.drulabs.syncroedit.model.User;
import org.drulabs.syncroedit.network.HttpOperations;
import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.PrefsHelper;
import org.drulabs.syncroedit.utils.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoteEditorActivity extends AppCompatActivity implements View.OnClickListener, View
        .OnFocusChangeListener, ChildEventListener, NoteItemAdapter.OnListInteractionListener {

    public static final String KEY_NOTE_ITEM = "note_parameter";

    //TODO change this limit to read config from firebase remote config
    private static final int NOTE_LIST_LIMIT = 20;

    private static final int REQ_ADD_CONTACT = 11;
    private static final int REQ_PICK_IMAGE = 12;
    private static final int REQ_CAPTURE_IMAGE = 13;

    private static final int CAMERA_PERMISSION_CODE = 3323;

    RecyclerView noteItemsRecyclerView;
    NoteItemAdapter noteItemsAdapter;
    EditText etNewItemData;
    EditText etNoteTitle;

    FloatingActionButton fabNoteItemDone;

    CircleImageView imageIcon;

    private PrefsHelper prefsHelper;

    private boolean isEditingItem = false;

    // This is required when editing an existing note, or else a new note id will be generated
    private String noteId = null;
    // This is required when editing other users note
    private String userId = null;

    private boolean isNewNote = false;

    private boolean isMyNote = true;

    /**
     * if this is null then no image is added, else it contains image local path
     */
    //private String imagePath = null;

    // Firebase variables
    private DatabaseReference mNotesDBRef;
    private DatabaseReference mUserDBRef;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        prefsHelper = PrefsHelper.getInstance(NoteEditorActivity.this);

        noteItemsRecyclerView = (RecyclerView) findViewById(R.id.rv_note_items);
        etNewItemData = (EditText) findViewById(R.id.et_new_note_item_data);
        etNoteTitle = (EditText) findViewById(R.id.et_new_note_title);
        etNoteTitle.setOnFocusChangeListener(this);
        etNoteTitle.requestFocus();

        fabNoteItemDone = (FloatingActionButton) findViewById(R.id.fab_add_note_item);
        fabNoteItemDone.setOnClickListener(this);
        imageIcon = (CircleImageView) findViewById(R.id.icon_note_image);
        imageIcon.setOnClickListener(this);

        LinearLayoutManager llm = new LinearLayoutManager(NoteEditorActivity.this);
        noteItemsRecyclerView.setLayoutManager(llm);
        noteItemsAdapter = new NoteItemAdapter(NoteEditorActivity.this, this);
        noteItemsRecyclerView.setAdapter(noteItemsAdapter);

        Bundle extras = getIntent().getExtras();

        // Get Remote Config instance.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        mFirebaseRemoteConfig.setDefaults(R.xml.remoteconfig_initial);
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    setRecyclerViewCustomSwipe();
                }
            }
        });

        setRecyclerViewCustomSwipe();

        /*if (getIntent().getAction().equalsIgnoreCase(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            Log.d("NoteEditor", "uri:" + uri.toString());
        } else*/
        if (extras != null && extras.containsKey(KEY_NOTE_ITEM)) {
            Note existingNote = (Note) extras.get(KEY_NOTE_ITEM);

            if (existingNote == null) {
                NotificationToast.showToast(NoteEditorActivity.this, "Invalid parameter received");
                NoteEditorActivity.this.finish();
                return;
            }

            noteId = existingNote.getId();
            //userId = existingNote.getCreatedByUserId();
            isNewNote = false;
            isMyNote = prefsHelper.getUserUID().equalsIgnoreCase(existingNote.getCreatedByUserId());
        }

        if (noteId == null) {
            // a new note is being created, generate note id and proceed
            noteId = prefsHelper.getUserUID() + System.currentTimeMillis();
            isNewNote = true;
            isMyNote = true;
        }

        if (userId == null) {
            // note creator is current user get user id from preferences
            userId = prefsHelper.getUserUID();
        }

        // Firebase db initialization
        mNotesDBRef = FirebaseDatabase.getInstance().getReference().child
                (Constants.NOTES_DATABASE).child(noteId);

        // Adding listener for existing note
        mNotesDBRef.addValueEventListener(noteInfoListener);

        //Adding listener for update in new note
        mNotesDBRef.child(Constants.NOTES_DB_ITEMS).addChildEventListener(this);

        mUserDBRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER_BASE)
                .child(userId).child(Constants.USER_BASE_NOTES);
        mUserDBRef.addChildEventListener(noteCollabListener);

        mStorageReference = mFirebaseStorage.getReferenceFromUrl("gs://" + Constants
                .NOTES_IMAGE_BUCKET);
        mStorageReference = mStorageReference.child(Constants.NOTES_IMAGE_FOLDER).child(noteId +
                ".jpg");

        long TEM_MEG = 20 * 1024 * 1024; // Max size of image to be downloaded
        mStorageReference.getBytes(TEM_MEG).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    imageIcon.setImageBitmap(bitmap);
                    imageIcon.setBackground(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_note_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.note_edit_done:
                saveNoteTitle();
                finish();
                return true;
            case R.id.note_edit_discard:
                finish();
                return true;
            case R.id.note_add_collaborators:
                if (!isMyNote) {
                    NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                            .not_note_admin_add_collaborators_msg));
                    return true;
                }
                boolean isNoteSaved = (!isNewNote) ? true : (etNoteTitle.getText().toString()
                        .trim().length() > 0 && !etNoteTitle.hasFocus());

                if (isNoteSaved) {
                    Intent pickContactIntent = new Intent(NoteEditorActivity.this,
                            ContactListActivity.class);
                    startActivityForResult(pickContactIntent, REQ_ADD_CONTACT);
                } else {
                    NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                            .empty_note_add_collaborator_error));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_ADD_CONTACT:

                    Bundle extraData = data.getExtras();
                    String selectedContact = extraData.getString(ContactListActivity
                            .KEY_CONTACT_NUMBER);
                    // hardcoding for testing
                    // selectedContact = "+919916367260";
                    DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.USER_BASE);
                    Query userQuery = userDBRef.orderByChild(Constants.USER_BASE_PHONE).equalTo
                            (selectedContact).limitToLast(1);
                    userQuery.addListenerForSingleValueEvent(phoneQueryEventListener);
                    break;
                case REQ_PICK_IMAGE:
                    if (data != null && data.getData() != null) {

                        Uri picUri = data.getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver
                                    (), picUri);
                            imageIcon.setImageBitmap(bitmap);
                            imageIcon.setBackground(null);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();

                            mStorageReference.putBytes(imageData).addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot
                                                                      taskSnapshot) {
                                            NotificationToast.showToast(NoteEditorActivity.this,
                                                    "Image uploaded successfully");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    NotificationToast.showToast(NoteEditorActivity.this, "Image " +
                                            "upload FAILED");
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQ_CAPTURE_IMAGE:
                    try {
                        //Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                        final File imageFile = new File(getExternalFilesDir(Environment
                                .DIRECTORY_PICTURES), noteId + ".jpg");
                        Uri imageUri = Uri.fromFile(imageFile);
                        Log.d("tttt", imageUri.toString());

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver
                                (), imageUri);
                        imageIcon.setImageBitmap(bitmap);
                        imageIcon.setBackground(null);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageData = baos.toByteArray();

                        mStorageReference.putBytes(imageData).addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        NotificationToast.showToast(NoteEditorActivity.this,
                                                "Image uploaded successfully");
                                        imageFile.delete();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        NotificationToast.showToast(NoteEditorActivity.this,
                                                "Image upload FAILED");
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }


    ChildEventListener noteCollabListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String removedNoteId = dataSnapshot.getValue().toString();
            if (noteId.equalsIgnoreCase(removedNoteId)) {
                NoteEditorActivity.this.finish();
                NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                        .note_collaborator_removed_msg));
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private ValueEventListener phoneQueryEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                User selectedUser = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    selectedUser = snapshot.getValue(User.class);
                }

                String apiKey = mFirebaseRemoteConfig.getString(Constants.KEY_FCM_API);
                if (apiKey.equalsIgnoreCase("aaa")) {
                    apiKey = Constants.FCM_API_KEY;
                }

                //Setting allowed user in notes
                mNotesDBRef.child(Constants.NOTES_DB_ALLOWED_USERS).push().setValue(selectedUser
                        .getUserId());

                //Setting note id in user's notes
                DatabaseReference selectedUserRef = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.USER_BASE).child(selectedUser.getUserId()).child(Constants
                                .USER_BASE_NOTES);
                final User finalSelectedUser = selectedUser;
                final String finalApiKey = apiKey;
                selectedUserRef.push().setValue(noteId).addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HttpOperations.sendNoteCollaborationRequest(finalSelectedUser
                                                .getFcmToken(), noteId, prefsHelper.getUserUID(),
                                        prefsHelper.getUserName(), etNoteTitle.getText()
                                                .toString(), finalApiKey);
                            }
                        });

            } else {
                NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                        .not_app_user_msg));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mNotesDBRef.removeEventListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_note_item:

                String data = etNewItemData.getText().toString();
                if (data != null && data.trim().length() != 0) {
                    if (!isEditingItem) {
                        if (noteItemsAdapter.getItemCount() <= NOTE_LIST_LIMIT) {
                            NoteItem singleItem = new NoteItem();
                            singleItem.setNoteItemId(noteId);
                            singleItem.setAddedByUser(prefsHelper.getUserName());
                            singleItem.setAddedByUserId(prefsHelper.getUserUID());
                            singleItem.setData(data);
                            saveNoteItem(singleItem);
                        } else {
                            NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                                    .list_limit_reached_msg));
                        }
                    } else {
                        selectedItem.setData(data);
                        updateNoteItem(selectedItem);
                    }
                    etNewItemData.setText("");

                } else {
                    NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                            .empty_note_item_msg));
                }
                break;
            case R.id.icon_note_image:
                boolean hasImage = (imageIcon.getDrawable() != null);
                if (!hasImage) {
                    askCameraPermission();
                } else {
                    //TODO rectify this
                    Utility.getLoadedImageOptionDialog(this, noteId).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_new_note_title:

                if (!hasFocus) {
                    String noteTitle = etNoteTitle.getText().toString();
                    if (noteTitle == null || noteTitle.trim().length() == 0) {
                        NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                                .note_title_empty_msg));
                        etNoteTitle.requestFocus();
                    } else {
                        saveOrUpdateNote();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        NoteItem singleItem = dataSnapshot.getValue(NoteItem.class);
        noteItemsAdapter.addItem(singleItem);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        NoteItem singleItem = dataSnapshot.getValue(NoteItem.class);
        noteItemsAdapter.updateItem(singleItem);

        mNotesDBRef.child(Constants.NOTES_DB_ITEMS).child(singleItem.getNoteItemId())
                .addValueEventListener(noteItemChangeListener);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        NoteItem singleItem = dataSnapshot.getValue(NoteItem.class);
        noteItemsAdapter.removeItem(singleItem);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        // since we are not working with ordered data, this method needs no definition whatsoever
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e("NoteEditor", "NoteEditorERROR: " + databaseError.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (permissions[0].equalsIgnoreCase(Manifest.permission.CAMERA) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onClick(imageIcon);
                } else {
                    NotificationToast.showToast(NoteEditorActivity.this, "Cannot launch camera");
                }
                break;
            default:
                break;
        }
    }

    private void askCameraPermission() {
        boolean isGranted = Utility.checkPermission(Manifest.permission.CAMERA,
                NoteEditorActivity.this);
        if (!isGranted) {
            Utility.requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE,
                    NoteEditorActivity.this);
        } else {
            Log.d("tttt", noteId);
            Utility.getImageSrcSelectionDialog(this, noteId, REQ_PICK_IMAGE, REQ_CAPTURE_IMAGE)
                    .show();
        }
    }

//    private void askSDCardPermission() {
//        boolean isGranted = Utility.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                NoteEditorActivity.this);
//        if (!isGranted) {
//            Utility.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    CAMERA_PERMISSION_CODE, NoteEditorActivity.this);
//        } else {
//            Utility.getImageSrcSelectionDialog(this, REQ_PICK_IMAGE, REQ_CAPTURE_IMAGE).show();
//        }
//    }

    private ValueEventListener noteInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //TODO again getting the entire data. add pagination logic here
            if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                Note existingNote = dataSnapshot.getValue(Note.class);
                if (existingNote != null) {
                    etNoteTitle.setText(existingNote.getTitle());
                }
            } else {
                if (!isNewNote) {
                    NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                            .admin_deleted_note_msg));
                    NoteEditorActivity.this.finish();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener noteItemChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            NoteItem singleNoteItem = dataSnapshot.getValue(NoteItem.class);
            noteItemsAdapter.updateItem(singleNoteItem);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("NoteEditor", "NoteItemChangeListener: DB ERROR: " + databaseError.toString());
        }
    };

    private void saveOrUpdateNote() {
        String title = etNoteTitle.getText().toString();
        Note newNote = new Note();
        newNote.setNoteType(Note.TYPE_LIST);
        newNote.setTitle(title);
        newNote.setLocalTimeStamp(System.currentTimeMillis());
        newNote.setData("Created by " + prefsHelper.getUserName());
        // newNote.addNoteItem(noteItems);
        newNote.setCreatedByUserId(prefsHelper.getUserUID());
        newNote.setCreatedByUser(prefsHelper.getUserName());
        // newNote.addAllowedUser(prefsHelper.getUserUID());
        newNote.setId(noteId);

        if (isNewNote) {
            saveNote(newNote);
        } else {
            updateNote(newNote);
        }
    }

    private void saveNoteTitle() {
        String noteTitle = etNoteTitle.getText().toString();
        Map<String, Object> noteTitleMap = new HashMap<>();
        noteTitleMap.put(Constants.NOTES_DB_TITLE, noteTitle);
        mNotesDBRef.updateChildren(noteTitleMap);
    }

    private void saveNote(final Note newNote) {
        mNotesDBRef.setValue(newNote)
                .addOnCompleteListener(NoteEditorActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        NotificationToast.showToast(NoteEditorActivity.this, getString(R
                                .string.note_saved_msg));
                        if (isNewNote) {
                            // Note saved successfully update the user's database
                            mUserDBRef.push().setValue(newNote.getId());
                        }
                    }
                }).addOnFailureListener(NoteEditorActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NotificationToast.showToast(NoteEditorActivity.this, getString(R
                        .string.note_save_Failure_msg));
            }
        });
        //adding allowed user in notes db
        //mNotesDBRef.child(Constants.NOTES_DB_ALLOWED_USERS).push().setValue()
    }

    private void updateNote(Note note) {
        mNotesDBRef.child(Constants.NOTES_DB_TITLE).setValue(note.getTitle());
        mNotesDBRef.child(Constants.NOTES_DB_TIMESTAMP).setValue(note.getLocalTimeStamp());
    }

    private void saveNoteItem(NoteItem noteItem) {
        DatabaseReference noteDBRef = mNotesDBRef.child(Constants.NOTES_DB_ITEMS).push();
        String noteItemId = noteDBRef.getKey();
        noteItem.setNoteItemId(noteItemId);
        noteDBRef.setValue(noteItem);
    }

    private void updateNoteItem(NoteItem noteItem) {
        DatabaseReference noteDBRef = mNotesDBRef.child(Constants.NOTES_DB_ITEMS).child(noteItem
                .getNoteItemId());
        noteItem.setTimestamp(System.currentTimeMillis());
        noteItem.setAddedByUser(prefsHelper.getUserName());
        noteItem.setAddedByUserId(prefsHelper.getUserUID());
        noteDBRef.setValue(noteItem);
        isEditingItem = false;
        selectedItem = null;
    }

    private void deleteNoteItem(NoteItem noteItem) {
        DatabaseReference noteDBRef = mNotesDBRef.child(Constants.NOTES_DB_ITEMS).child(noteItem
                .getNoteItemId());
        //noteItem.setTimestamp(System.currentTimeMillis());
        //noteItem.setAddedByUser(prefsHelper.getUserName());
        //noteItem.setAddedByUserId(prefsHelper.getUserUID());
        noteDBRef.removeValue();
        NotificationToast.showToast(NoteEditorActivity.this, getString(R.string
                .note_item_deleted_msg));
    }

    private NoteItem selectedItem;

    @Override
    public void OnNoteItemClicked(NoteItem noteItem) {
        isEditingItem = true;
        selectedItem = noteItem;
        etNewItemData.setText(noteItem.getData());
    }

    //Sets custom swipe on recycler view
    private void setRecyclerViewCustomSwipe() {
        ItemTouchHelper.SimpleCallback simpleTouchCallback = new ItemTouchHelper.SimpleCallback
                (0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                NoteItem swipedItem = noteItemsAdapter.getDisplayList().get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    deleteNoteItem(swipedItem);
                } else {
                    OnNoteItemClicked(swipedItem);
                }
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView
                    .ViewHolder viewHolder, float dX, float dY, int actionState, boolean
                                            isCurrentlyActive) {

                Paint paint = new Paint();
                Bitmap displayIcon;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float itemHeight = (float) itemView.getBottom() - (float) itemView.getTop();
                    float iconWidth = itemHeight / 3;

                    int deleteColor = Color.parseColor(mFirebaseRemoteConfig.getString
                            (Constants.KEY_ABOUT_DELETE_COLOR));
                    int editColor = Color.parseColor(mFirebaseRemoteConfig.getString
                            (Constants.KEY_ABOUT_EDIT_COLOR));

                    if (dX > 0) {
                        paint.setColor(editColor);
                        RectF bgDelete = new RectF((float) itemView.getLeft(), (float) itemView
                                .getTop(), dX, (float) itemView.getBottom());
                        canvas.drawRect(bgDelete, paint);
                        displayIcon = BitmapFactory.decodeResource(getResources(), R.mipmap
                                .ic_edit);
                        RectF displayIconDest = new RectF((float) itemView.getLeft() + iconWidth,
                                (float) itemView.getTop() + iconWidth, (float) itemView.getLeft() +
                                2 * iconWidth, (float) itemView.getBottom() - iconWidth);
                        canvas.drawBitmap(displayIcon, null, displayIconDest, paint);
                    } else {
                        paint.setColor(deleteColor);
                        RectF bgEdit = new RectF((float) itemView.getRight() + dX, (float) itemView
                                .getTop(), (float) itemView.getRight(), (float) itemView
                                .getBottom());
                        canvas.drawRect(bgEdit, paint);
                        displayIcon = BitmapFactory.decodeResource(getResources(), R.mipmap
                                .ic_trash_can);
                        RectF displayIconDest = new RectF((float) itemView.getRight() - 2 *
                                iconWidth, (float) itemView.getTop() + iconWidth, (float) itemView
                                .getRight() - iconWidth, (float) itemView.getBottom() - iconWidth);
                        canvas.drawBitmap(displayIcon, null, displayIconDest, paint);
                    }

                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleTouchCallback);
        itemTouchHelper.attachToRecyclerView(noteItemsRecyclerView);
    }
}