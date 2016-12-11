package org.drulabs.syncroedit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.model.NoteItem;
import org.drulabs.syncroedit.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored by KaushalD on 9/28/2016.
 */

public class NoteItemAdapter extends RecyclerView.Adapter<NoteItemAdapter.NoteItemHolder> {

    private static final int TYPE_MINE = 0;
    private static final int TYPE_OTHER = 1;

    private List<NoteItem> items;
    private PrefsHelper prefsHelper;

    private OnListInteractionListener mListener;

    public NoteItemAdapter(@NonNull Context context, @NonNull OnListInteractionListener listener) {
        prefsHelper = PrefsHelper.getInstance(context);
        items = new ArrayList<>();
        mListener = listener;
    }

    public NoteItemAdapter(@NonNull Context context, @NonNull List<NoteItem> items, @NonNull
            OnListInteractionListener listener) {
        this.items = items;
        prefsHelper = PrefsHelper.getInstance(context);
        mListener = listener;
    }

    @Override
    public NoteItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View noteItemView = LayoutInflater.from(context).inflate(R.layout
                .layout_note_item, parent, false);
        if (viewType == TYPE_MINE) {
            noteItemView.setBackgroundResource(R.drawable.bg_noteitem_mine);
        } else {
            noteItemView.setBackgroundResource(R.drawable.bg_noteitem_other);
        }
        return new NoteItemHolder(noteItemView);
    }

    @Override
    public void onBindViewHolder(NoteItemHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        int count = (items == null) ? 0 : items.size();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        NoteItem item = items.get(position);
        int viewType = (item.getAddedByUserId().equalsIgnoreCase(prefsHelper.getUserUID())) ?
                TYPE_MINE : TYPE_OTHER;
        return viewType;
    }

    public void addItem(NoteItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void addItem(List<NoteItem> noteItems) {
        items.addAll(noteItems);
        notifyDataSetChanged();
    }

    public void updateItem(NoteItem item) {
        int itemIndex = items.indexOf(item);
        if (itemIndex >= 0 && itemIndex < items.size()) {
            items.remove(item);
            items.add(itemIndex, item);
        }
        notifyDataSetChanged();
    }

    public void removeItem(NoteItem item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void removeItem(int itemIndex) {
        if (itemIndex > 0 && itemIndex < getItemCount()) {
            items.remove(itemIndex);
            notifyDataSetChanged();
        }
    }

    public List<NoteItem> getDisplayList() {
        return items;
    }

    public class NoteItemHolder extends RecyclerView.ViewHolder {
        TextView itemContent;
        TextView userName;

        public NoteItemHolder(View itemView) {
            super(itemView);
            itemContent = (TextView) itemView.findViewById(R.id.tv_note_item_content);
            userName = (TextView) itemView.findViewById(R.id.tv_note_item_by);
        }

        public void bind(final NoteItem item) {
            itemContent.setText(item.getData());
            userName.setText(item.getAddedByUser());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnNoteItemClicked(item);
                }
            });
        }
    }

    public interface OnListInteractionListener {
        void OnNoteItemClicked(NoteItem noteItem);
    }

}
