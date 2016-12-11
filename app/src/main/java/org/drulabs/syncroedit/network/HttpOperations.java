package org.drulabs.syncroedit.network;

import android.util.Log;

import org.drulabs.syncroedit.config.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Authored by KaushalD on 10/12/2016.
 */

public class HttpOperations {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendNoteCollaborationRequest(final String singleTargetToken, final String
            noteId, final String userId, final String userName, final String noteTitle, final
                                                    String apiKey) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    JSONArray regIdArray = new JSONArray();
                    regIdArray.put(singleTargetToken);

                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_FCM_TYPE, Constants.TYPE_NOTE_COLLAB);
                    data.put(Constants.KEY_NOTE_ID, noteId);
                    data.put(Constants.KEY_USER_ID, userId);
                    data.put(Constants.KEY_NOTE_TITLE, noteTitle);
                    data.put(Constants.KEY_USER_NAME, userName);

                    JSONObject rootObj = new JSONObject();
                    rootObj.put(Constants.FCM_REG_IDS, regIdArray);
                    rootObj.put(Constants.FCM_DATA, data);

                    RequestBody requestBody = RequestBody.create(JSON, rootObj.toString());

//                    RequestBody requestBody = new FormBody.Builder()
//                            .add(Constants.FCM_REG_IDS, regIdArray.toString())
//                            .add(Constants.FCM_DATA, data.toString())
//                            .build();

                    Request okhttpRequest = new Request.Builder()
                            .url(Constants.FCM_URL)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "key=" + apiKey)
                            .post(requestBody)
                            .build();

                    client.newCall(okhttpRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d("CollabRequest:", response.toString());
                            response.close();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

}
