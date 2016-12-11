package org.drulabs.syncroedit.model;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import org.drulabs.syncroedit.R;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Authored by KaushalD on 9/26/2016.
 */

public class Note implements Serializable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_LIST, TYPE_CANVAS, TYPE_PARA, TYPE_PIC})
    public @interface NoteMode {
    }

    //Note types
    public static final int TYPE_LIST = 1;
    public static final int TYPE_PARA = 2;
    public static final int TYPE_PIC = 3;
    public static final int TYPE_CANVAS = 4;

    @IntDef({R.mipmap.ic_list_img, R.mipmap.ic_pic_image, R.mipmap.ic_para_img, R.mipmap.ic_canvas_img})
    public @interface NoteTypeImage {
    }

    @NoteTypeImage
    public static int getTypeImageId(@NoteMode int noteType) {
        switch (noteType) {
            case TYPE_LIST:
                return R.mipmap.ic_list_img;
            case TYPE_CANVAS:
                return R.mipmap.ic_canvas_img;
            case TYPE_PARA:
                return R.mipmap.ic_para_img;
            case TYPE_PIC:
                return R.mipmap.ic_pic_image;
        }
        return R.mipmap.ic_list_img;
    }

    @NoteMode
    public static int getType(int num) {
        switch (num) {
            case TYPE_LIST:
                return TYPE_LIST;
            case TYPE_CANVAS:
                return TYPE_CANVAS;
            case TYPE_PARA:
                return TYPE_PARA;
            case TYPE_PIC:
                return TYPE_PIC;
            default:
                throw new IllegalArgumentException("Unknown note type: " + num);
        }
    }

    private String id;
    // default is list type
    private int noteType = TYPE_LIST;
    private String title;
    private String noteURL = null;
    private String data = null;

    @Exclude
    private List<NoteItem> items = new ArrayList<>();
    private long localTimeStamp = System.currentTimeMillis();
    private String createdByUserId;
    private String createdByUser;

    @Exclude
    private List<String> allowedUsers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNoteURL() {
        return noteURL;
    }

    public void setNoteURL(String noteURL) {
        this.noteURL = noteURL;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Exclude
    public List<NoteItem> getItems() {
        return items;
    }

    public void addNoteItem(NoteItem noteItem) {
        items.add(noteItem);
    }

    public void addNoteItem(List<NoteItem> noteItems) {
        items.addAll(noteItems);
    }

    public void removeNoteItem(NoteItem noteItem) {
        items.remove(noteItem);
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void addAllowedUser(String userId) {
        if (!allowedUsers.contains(userId)) {
            allowedUsers.add(userId);
        }
    }

    public void addAllowedUser(List<String> users) {
        allowedUsers.addAll(users);
    }

    public void removeAllowedUser(String userId) {
        allowedUsers.remove(userId);
    }

    @NoteMode
    public int getNoteType() {
        return noteType;
    }

    public void setNoteType(@NoteMode int noteType) {
        this.noteType = noteType;
    }

    public long getLocalTimeStamp() {
        return localTimeStamp;
    }

    public void setLocalTimeStamp(long localTimeStamp) {
        this.localTimeStamp = localTimeStamp;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(@NonNull String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;

        Note note = (Note) o;

        if (getNoteType() != note.getNoteType()) return false;
        if (!getId().equals(note.getId())) return false;
        return getCreatedByUserId().equals(note.getCreatedByUserId());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getNoteType();
        result = 31 * result + getCreatedByUserId().hashCode();
        return result;
    }
}
