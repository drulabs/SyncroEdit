package org.drulabs.syncroedit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.fragments.NotesFragment;
import org.drulabs.syncroedit.model.Note;
import org.drulabs.syncroedit.utils.PrefsHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> mNotes;
    private final NotesFragment.OnListFragmentInteractionListener mListener;

    private SimpleDateFormat simpleDateFormat;
    private String mCurrentUserID;

    public NotesAdapter(@NonNull Context cxt, @NonNull List<Note> notes, NotesFragment
            .OnListFragmentInteractionListener listener) {
        this.mCurrentUserID = PrefsHelper.getInstance(cxt).getUserUID();
        mNotes = notes;
        mListener = listener;
        simpleDateFormat = new SimpleDateFormat("MMM-dd-yyyy, HH:mm:ss");
    }

    public NotesAdapter(@NonNull Context cxt, NotesFragment.OnListFragmentInteractionListener
            listener) {
        this.mCurrentUserID = PrefsHelper.getInstance(cxt).getUserUID();
        mNotes = new ArrayList<>();
        mListener = listener;
        simpleDateFormat = new SimpleDateFormat("MMM-dd-yyyy, HH:mm:ss");
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_single_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, int position) {
        Note singleNote = mNotes.get(position);
        holder.bind(singleNote);
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public void addNote(@NonNull Note note) {
        if (mNotes.contains(note)) {
            updateNote(note);
        } else {
            mNotes.add(note);
        }
        notifyDataSetChanged();
    }

    public void addNote(@NonNull List<Note> notes) {
        mNotes.addAll(notes);
        notifyDataSetChanged();
    }

    public void updateNote(@NonNull Note note) {
        int noteIndex = mNotes.indexOf(note);
        if (noteIndex >= 0 && noteIndex < mNotes.size()) {
            mNotes.remove(note);
            mNotes.add(noteIndex, note);
        }
        notifyDataSetChanged();
    }

    public void removeNote(@NonNull Note note) {
        mNotes.remove(note);
        notifyDataSetChanged();
    }

    public void removeNote(@NonNull String noteId) {
        Note noteToBeRemoved = null;
        for (Note singleNote : mNotes) {
            if (noteId.equalsIgnoreCase(singleNote.getId())) {
                noteToBeRemoved = singleNote;
                break;
            }
        }
        mNotes.remove(noteToBeRemoved);
        notifyDataSetChanged();
    }

    public List<Note> getNoteList() {
        return mNotes;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        public final View mRootView;
        public final ImageView mNoteTypeImage;
        public final TextView mNoteTitle;
        public final TextView mCreatedBy;
        public final TextView mTimestamp;
        public final View mImgShared;

        public NoteViewHolder(View view) {
            super(view);
            mRootView = view;
            mNoteTypeImage = (ImageView) view.findViewById(R.id.nn_note_type_image);
            mNoteTitle = (TextView) view.findViewById(R.id.nn_note_title);
            mCreatedBy = (TextView) view.findViewById(R.id.nn_note_created_by);
            mTimestamp = (TextView) view.findViewById(R.id.nn_note_timestamp);
            mImgShared = view.findViewById(R.id.img_shared);
        }

        public void bind(final Note noteItem) {
            mNoteTypeImage.setImageResource(Note.getTypeImageId(noteItem.getNoteType()));
            mNoteTitle.setText(noteItem.getTitle());
            mCreatedBy.setText(noteItem.getCreatedByUser());
            mTimestamp.setText(simpleDateFormat.format(new Date(noteItem.getLocalTimeStamp())));
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view != null && mListener != null) {
                        mListener.onListFragmentInteraction(noteItem);
                    }
                }
            });
            boolean isCurrentUserNote = mCurrentUserID.equalsIgnoreCase(noteItem
                    .getCreatedByUserId());
            mImgShared.setVisibility(isCurrentUserNote ? View.GONE : View.VISIBLE);
            mNoteTypeImage.setAlpha((isCurrentUserNote) ? 0.9f : 0.4f);
        }
    }
}
