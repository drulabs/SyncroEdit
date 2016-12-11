package org.drulabs.syncroedit.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.adapters.NotesAdapter;
import org.drulabs.syncroedit.config.Constants;
import org.drulabs.syncroedit.model.Note;
import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.PrefsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class NotesFragment extends Fragment {

    // TODO: Customize parameters
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotesFragment() {
        userNoteIds = new HashMap<>();
    }

    private NotesAdapter notesAdapter;

    private PrefsHelper prefsHelper;

    private RecyclerView rvNotes;
    private View noteStatusHolder;

    private Map<String, String> userNoteIds;


    // Firebase variables
    private DatabaseReference mNotesDBRef;
    private DatabaseReference mUserDBRef;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    // TODO: Customize parameter initialization
    public static NotesFragment newInstance() {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notesAdapter = new NotesAdapter(getActivity(), mListener);

        // Firebase db initialization
        mNotesDBRef = FirebaseDatabase.getInstance().getReference().child
                (Constants.NOTES_DATABASE);
        mUserDBRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER_BASE)
                .child(prefsHelper.getUserUID()).child(Constants.USER_BASE_NOTES);
//        mUserDBRefOther = mUserDBRef.getParent().child(Constants.USER_BASE_OTHER);
//        mUserDBRef = mUserDBRef.child(Constants.USER_BASE_NOTES);

        //Initialize remote config
        initializeRemoteConfig();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        noteStatusHolder = view.findViewById(R.id.note_status_holder);

        // Set the adapter
        //if (view instanceof RecyclerView) {
        Context context = view.getContext();
        rvNotes = (RecyclerView) view.findViewById(R.id.list_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(context));
        rvNotes.setAdapter(notesAdapter);
        setRecyclerViewCustomSwipe();
        //}

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO this gets all the notes, add pagination logic later
        mUserDBRef.addChildEventListener(userNoteChildEventListener);

        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    setRecyclerViewCustomSwipe();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mUserDBRef.removeEventListener(userNoteChildEventListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

        prefsHelper = PrefsHelper.getInstance(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initializeRemoteConfig() {
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
    }

    private ValueEventListener noteChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Note singleNote = dataSnapshot.getValue(Note.class);
            notesAdapter.updateNote(singleNote);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("NotesFrag", "NoteItemChangeListener: DB ERROR: " + databaseError.toString());
        }
    };

    private ChildEventListener userNoteChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            NoteRetrievedCallback noteAddedCallback = new NoteRetrievedCallback() {
                @Override
                public void onNoteRetrieved(Note note) {
                    notesAdapter.addNote(note);
                    if (rvNotes.getVisibility() == View.GONE) {
                        rvNotes.setVisibility(View.VISIBLE);
                        noteStatusHolder.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String noteIdKey, DatabaseError error) {
                    Log.e("NoteFrag", "onChildAdded ERROR: " + error);
                    // Note not found, remove from user's notes
                    mUserDBRef.child(noteIdKey).removeValue();
                }
            };
            String noteId = dataSnapshot.getValue().toString();
            String noteIdKey = dataSnapshot.getKey();
            userNoteIds.put(noteIdKey, noteId);
            getNoteInfo(noteId, noteIdKey, noteAddedCallback);
            mNotesDBRef.child(noteId).addValueEventListener(noteChangeListener);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            // this will not happen as per our usecase
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String noteId = dataSnapshot.getValue().toString();
            String noteIdKey = dataSnapshot.getKey();
            userNoteIds.remove(noteIdKey);
            notesAdapter.removeNote(noteId);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            //Not dealing with ordered data so forget about this for now
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("NotesFrag", "userNoteChildEventListener: DB ERROR: " + databaseError.toString());
        }
    };

    private void getNoteInfo(final String noteId, final String noteIdKey, final
    NoteRetrievedCallback noteRetrievedCallback) {
        Log.d("NotesFrag", "noteId: " + noteId);
        mNotesDBRef.child(noteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Note singleNote = dataSnapshot.getValue(Note.class);
                if (singleNote != null) {
                    noteRetrievedCallback.onNoteRetrieved(singleNote);
                } else {
                    noteRetrievedCallback.onError(noteId, null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("NotesFrag", "DB ERROR: " + databaseError.toString());
                noteRetrievedCallback.onError(noteIdKey, databaseError);
            }
        });
    }

    private void deleteNote(Note note) {
        String myUUID = prefsHelper.getUserUID();
        String noteCreatorUserId = note.getCreatedByUserId();
        boolean isMyNote = myUUID.equalsIgnoreCase(noteCreatorUserId);

        //delete from user's note list
        String noteIdKey = getUserNoteKey(note.getId());
        if (noteIdKey != null) {
            mUserDBRef.child(noteIdKey).removeValue();
        }

        if (isMyNote) {
            // remove from notes db if it is current user's note
            mNotesDBRef.child(note.getId()).removeValue();
            NotificationToast.showToast(getActivity(), getString(R.string.note_deleted_msg));
        } else {
            // this note will not be deleted from notes db if current user is not the creator or
            // admin of the note
            NotificationToast.showToast(getActivity(), getString(R.string
                    .note_collaborator_removed_msg));
        }
    }

//    private void deleteNoteCollaborators(String noteId){
//        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference()
//                .child(Constants.USER_BASE);
//        Query userQuery = userDBRef.orderByChild(Constants.USER_BASE_NOTES+"/"+noteId).equalTo
//                (selectedContact).limitToLast(1);
//        userQuery.addListenerForSingleValueEvent(phoneQueryEventListener);
//    }

    private String getUserNoteKey(String noteId) {
        for (Map.Entry entry : userNoteIds.entrySet()) {
            if (noteId.equalsIgnoreCase(entry.getValue().toString())) {
                return entry.getKey().toString();
            }
        }
        return null;
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
                Note swipedNote = notesAdapter.getNoteList().get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    deleteNote(swipedNote);
                } else {
                    //Noted edit happening on right swipe
                    mListener.onListFragmentInteraction(swipedNote);
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
                        RectF bgDelete = new RectF((float) itemView.getLeft(), (float) itemView.getTop
                                (), dX, (float) itemView.getBottom());
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
                                .getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
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
        itemTouchHelper.attachToRecyclerView(rvNotes);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Note note);
    }

    private interface NoteRetrievedCallback {
        void onNoteRetrieved(Note note);

        void onError(String noteIdKey, DatabaseError error);
    }
}
