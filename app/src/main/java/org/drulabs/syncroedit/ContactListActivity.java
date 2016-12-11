package org.drulabs.syncroedit;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.drulabs.syncroedit.fragments.ContactsFragment;
import org.drulabs.syncroedit.model.DeviceContact;
import org.drulabs.syncroedit.notification.NotificationToast;
import org.drulabs.syncroedit.utils.Utility;

public class ContactListActivity extends AppCompatActivity implements ContactsFragment
        .OnContactClickedListener, SearchView.OnQueryTextListener {

    private static final int READ_CONTACT_REQ_CODE = 21;

    public static final String KEY_CONTACT_NUMBER = "device_contact_num";
    public static final String KEY_CONTACT_NAME = "contact_name";

    public static final int MAX_PHONE_LENGTH = 10;

    private ContactsFragment contactsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        contactsFragment = new ContactsFragment();
        checkReadContactPermission();
        handleSearchIntent(getIntent());

        setToolBarTitle(R.string.contacts);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equalsIgnoreCase(intent.getAction())) {
            String queryText = intent.getStringExtra(SearchManager.QUERY);
            // perform search
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contacts_list, menu);

        MenuItem searchItem = menu.findItem(R.id.contact_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }

        return true;
    }

    @Override
    public void onContactSelected(DeviceContact contact) {

        String contactNum = getContactNumber(String.valueOf(contact.getContactId()));

        contactNum = contactNum.replaceAll("\\D+", "");
        int subStringStart = 0;

        int contactNumLen = contactNum.length();

        if (contactNumLen > MAX_PHONE_LENGTH) {
            subStringStart = contactNumLen - MAX_PHONE_LENGTH;
        }

        contactNum = contactNum.substring(subStringStart);

        contact.setPhone(contactNum);

        Bundle data = new Bundle();
        data.putString(KEY_CONTACT_NUMBER, contact.getPhone());
        data.putString(KEY_CONTACT_NAME, contact.getDisplayName());
        Intent result = new Intent();
        result.putExtras(data);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            NotificationToast.showToast(ContactListActivity.this, getString(R.string
                    .contact_read_failed_msg));
            finish();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contacts_fragment_host, contactsFragment)
                    .commitAllowingStateLoss();
        }
    }

    private void checkReadContactPermission() {
        boolean isGranted = Utility.checkPermission(Manifest.permission.READ_CONTACTS, this);
        if (!isGranted) {
            Utility.requestPermission(Manifest.permission.READ_CONTACTS, READ_CONTACT_REQ_CODE,
                    this);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contacts_fragment_host, contactsFragment)
                    .commitAllowingStateLoss();
        }
    }

    private String getContactNumber(String contactId) {
        String phoneNo = null;

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new
                        String[]{contactId}, null);

        while (cursor.moveToNext()) {
            phoneNo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds
                    .Phone.NUMBER));
        }

        cursor.close();
        return phoneNo;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        contactsFragment.filterContacts(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String text) {
        contactsFragment.filterContacts(text);
        return true;
    }

    private void setToolBarTitle(int resId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(resId));
        }
    }

//    private String getContactName(String contactId) {
//
//        String contactName = null;
//
//        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new
//                String[]{ContactsContract.Contacts.DISPLAY_NAME}, ContactsContract.Contacts
//                ._ID + " = ?", new String[]{String.valueOf(contactId)}, null);
//
//        if (cursor.moveToFirst()) {
//            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//        }
//        cursor.close();
//
//        return contactName;
//    }
}
