package org.drulabs.syncroedit.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drulabs.syncroedit.R;
import org.drulabs.syncroedit.adapters.ContactsAdapter;
import org.drulabs.syncroedit.model.DeviceContact;


public class ContactsFragment extends Fragment {

    private static final int LOADER_ID = 3;

    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    // TODO: Implement a more advanced example that makes use of this
    private static final String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0";

    // Defines a variable for the search string
    private String mSearchString = null;
    // Defines the array to hold values that replace the ?
    // private String[] mSelectionArgs = {mSearchString};

    private RecyclerView mContactListView;

    private OnContactClickedListener mListener;

    private ContactsAdapter contactsAdapter;

    // Empty public constructor, required by the system
    public ContactsFragment() {
    }

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View root = inflater.inflate(R.layout.contacts_list, container, false);
        mContactListView = (RecyclerView) root.findViewById(R.id.rv_contact_list);
        mContactListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactListView.setItemAnimator(new DefaultItemAnimator());
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initially filter text is null, this will show all contacts
        String filterText = null;
        filterContacts(filterText);
    }

    public void filterContacts(String filterText) {

        mSearchString = SELECTION;
        if (filterText != null && filterText.trim().length() > 0) {
            String selectionString = " AND " + ContactsContract.Contacts
                    .DISPLAY_NAME_PRIMARY + " LIKE \"%" + filterText + "%\" COLLATE NOCASE";
            mSearchString += selectionString;
        }

//        if (getLoaderManager().getLoader(LOADER_ID) != null) {
//            getLoaderManager().getLoader(LOADER_ID).cancelLoad();
//        }
        // Initializes a loader for loading the contacts
        getLoaderManager().restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

//                Uri contentUri = Uri.withAppendedPath(
//                        ContactsContract.Contacts.CONTENT_FILTER_URI,
//                        Uri.encode(mSearchString));
                // Starts the query
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.Contacts.CONTENT_URI,
                        PROJECTION,
                        mSearchString,
                        null,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> objectLoader, Cursor c) {
                // Put the result Cursor in the adapter for the ListView

//                if (mContactListView.getAdapter() == null) {
                contactsAdapter = new ContactsAdapter(c, mListener);
                mContactListView.setAdapter(contactsAdapter);
//                } else {
//                    contactsAdapter = null;
//                    contactsAdapter = new ContactsAdapter(c, mListener);
//                    contactsAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactClickedListener) {
            mListener = (OnContactClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactClickedListener");
        }
    }

    public interface OnContactClickedListener {
        void onContactSelected(DeviceContact contact);
    }
}
