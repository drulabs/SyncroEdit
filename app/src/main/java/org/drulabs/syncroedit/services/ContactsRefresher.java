package org.drulabs.syncroedit.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Authored by KaushalD on 10/12/2016.
 */

public class ContactsRefresher extends IntentService {

    private static final String[] PROJECTION = new String[]{ContactsContract.Contacts
            .DISPLAY_NAME_PRIMARY, ContactsContract.Contacts._ID};
    private static final String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0";

    public ContactsRefresher(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                PROJECTION, SELECTION, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " " +
                        "ASC");

        int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

        while (cursor.moveToNext()) {
            String id = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);

            Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds
                    .Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone._ID + " = " +
                    "?", new String[]{id}, null);

            int phoneNumIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                    .NUMBER);
            int typeIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                    .TYPE);

            while (phoneCursor.moveToNext()) {
                String phNum = phoneCursor.getString(phoneNumIndex);
            }

        }
    }
}
