package org.drulabs.syncroedit.model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Authored by KaushalD on 10/9/2016.
 */

public class DeviceContact implements Serializable {

    private String phone;
    private long contactId;
    private Uri profilePic;
    private String displayName;
    private boolean isAppUser = false;

    public Uri getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Uri profilePic) {
        this.profilePic = profilePic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public boolean isAppUser() {
        return isAppUser;
    }

    public void setAppUser(boolean appUser) {
        isAppUser = appUser;
    }
}
