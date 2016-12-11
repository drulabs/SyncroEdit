package org.drulabs.syncroedit.adapters;

import android.content.ContentUris;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.fragments.ContactsFragment;
import org.drulabs.syncroedit.model.DeviceContact;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private Cursor mCursor;
    private final int mNameColIdx, mIdColIdx;
    private ContactsFragment.OnContactClickedListener mListener;

    public ContactsAdapter(Cursor cursor, ContactsFragment.OnContactClickedListener listener) {
        mCursor = cursor;
        this.mListener = listener;
        mNameColIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
        mIdColIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        //mIdPhone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int pos) {

        View listItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_list_item, parent, false);

        return new ContactViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int pos) {
        // Extract info from cursor
        mCursor.moveToPosition(pos);
        String contactName = mCursor.getString(mNameColIdx);
        long contactId = mCursor.getLong(mIdColIdx);
        //String phone = mCursor.getString(mIdPhone);

        // Create contact model and bind to viewholder
        DeviceContact c = new DeviceContact();
        c.setDisplayName(contactName);
        c.setProfilePic(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                contactId));
        c.setContactId(contactId);
        //c.setPhone(phone);

        contactViewHolder.bind(c);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mImage;
        private TextView mLabel;
        private DeviceContact mBoundContact; // Can be null

        public ContactViewHolder(final View itemView) {
            super(itemView);
            mImage = (CircleImageView) itemView.findViewById(R.id.rounded_iv_profile);
            mLabel = (TextView) itemView.findViewById(R.id.tv_label);
        }

        public void bind(final DeviceContact contact) {
            mBoundContact = contact;
            mLabel.setText(contact.getDisplayName());
            Picasso.with(itemView.getContext())
                    .load(contact.getProfilePic())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_pic_image)
                    .into(mImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBoundContact != null) {
                        mListener.onContactSelected(contact);
                    }
                }
            });
        }

    }
}