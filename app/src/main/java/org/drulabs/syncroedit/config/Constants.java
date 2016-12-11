package org.drulabs.syncroedit.config;

/**
 * Authored by KaushalD on 9/26/2016.
 */

public interface Constants {

    // Keys and related items
    String USER_BASE = "user_base";
    String USER_BASE_NOTES = "notes";
    String USER_BASE_PHONE = "phone";
    String NOTES_DATABASE = "user_notes";
    String NOTES_DB_ITEMS = "items";
    String NOTES_DB_TITLE = "title";
    String NOTES_DB_TIMESTAMP = "localTimeStamp";
    String NOTES_DB_ALLOWED_USERS = "allowed_users";
    String KEY_FCM_TOKEN = "fcmToken";
    String NOTES_IMAGE_BUCKET = "holycrab-528ac.appspot.com";
    String NOTES_IMAGE_FOLDER = "note_images";

    // Limits, app id and other constants
    /**
     * Use this for pagination
     */
    int SINGLE_FETCH_LIMIT = 20;
    String USER_BASE_SERVICES = "services";

    // FCM constants
    String KEY_FCM_API = "API_KEY";
    String FCM_API_KEY = "AIzaSyDTStcNkYlSRgwqNIRQSAe3iJDuA5cruK0";
    String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    String FCM_REG_IDS = "registration_ids";
    String FCM_DATA = "data";

    // FCM remote config
    String KEY_ABOUT_DELETE_COLOR = "about_delete_color";
    String KEY_ABOUT_EDIT_COLOR = "about_edit_color";

    // Cloud messaging related
    int TYPE_NOTE_COLLAB = 1;

    // Other keys and constants
    String KEY_NOTE_ID = "note_id";
    String KEY_USER_ID = "user_id";
    String KEY_NOTE_TITLE = "note_title";
    String KEY_USER_NAME = "user_name";
    String KEY_FCM_TYPE = "cloud_message_type";
}