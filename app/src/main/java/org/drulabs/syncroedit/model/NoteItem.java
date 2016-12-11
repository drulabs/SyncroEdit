package org.drulabs.syncroedit.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

/**
 * Authored by KaushalD on 9/27/2016.
 */

public class NoteItem implements Serializable {

    private String noteItemId;
    private String data;
    private long timestamp = System.currentTimeMillis();
    private String addedByUserId;
    private String addedByUser;

    public String getNoteItemId() {
        return noteItemId;
    }

    public void setNoteItemId(String noteItemId) {
        this.noteItemId = noteItemId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddedByUserId() {
        return addedByUserId;
    }

    public void setAddedByUserId(String addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public String getAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(String addedByUser) {
        this.addedByUser = addedByUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteItem)) return false;

        NoteItem noteItem = (NoteItem) o;

        return getNoteItemId().equals(noteItem.getNoteItemId());

    }

    @Override
    public int hashCode() {
        return getNoteItemId().hashCode();
    }

    @Override
    public String toString() {
        String jsonRep = (new Gson()).toJson(this);
        return jsonRep;
    }

    public static NoteItem fromJSON(String jsonRep) {
        Gson gson = new Gson();
        NoteItem obj = gson.fromJson(jsonRep, NoteItem.class);
        return obj;
    }

    public static List<NoteItem> fromJSONArray(String jsonRep) {
        Gson gson = new Gson();
        List<NoteItem> obj = gson.fromJson(jsonRep, new TypeToken<List<NoteItem>>() {
        }.getType());
        return obj;
    }
}
