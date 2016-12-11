package org.drulabs.syncroedit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.drulabs.syncroedit.NoteImageActivity;
import org.drulabs.syncroedit.notification.NotificationToast;

import java.io.File;

/**
 * Authored by KaushalD on 8/27/2016.
 */
public class Utility {

    public static void requestPermission(String strPermission, int perCode, Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, strPermission)) {
            NotificationToast.showToast(activity, strPermission + " is required for app to " +
                    "function");
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{strPermission}, perCode);
        }
    }

    public static boolean checkPermission(String strPermission, Context _c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(_c, strPermission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static AlertDialog getImageSrcSelectionDialog(final Activity activity, final String
            noteId, final int galleryCode, final int cameraCode) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("Insert Image to Note");
        String[] types = {"Capture using camera", "Pick from gallery"};
        alertDialog.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                switch (which) {
                    case 0:
                        //TODO Write image capture from camera code here

                        File imageFile = null;
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
                            try {
                                imageFile = new File(activity.getExternalFilesDir(Environment
                                        .DIRECTORY_PICTURES), noteId + ".jpg");
                                if (imageFile != null) {
//                                    Uri photoUri = FileProvider.getUriForFile(activity, activity
//                                            .getPackageName(), imageFile);
                                    Uri photoUri = Uri.fromFile(imageFile);
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                    Log.d("tttt", photoUri.toString());
                                    activity.startActivityForResult(cameraIntent, cameraCode);
                                }
                            } catch (Exception e) {
                                NotificationToast.showToast(activity, "Error in creating image " +
                                        "file: " + e.toString());
                            }
                        }
                        break;
                    case 1:
                        Intent galleryIntent = new Intent();
                        galleryIntent.setType("image/*");
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        activity.startActivityForResult(Intent.createChooser(galleryIntent,
                                "Select Picture"), galleryCode);
                        break;
                }
            }

        });

        return (alertDialog.create());

    }

    public static AlertDialog getLoadedImageOptionDialog(final Activity activity, final String
            noteId) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("Note image options");
        String[] types = {"View image", "Remove"};
        alertDialog.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                switch (which) {
                    case 0:
                        Intent viewImageIntent = new Intent(activity, NoteImageActivity.class);
                        viewImageIntent.putExtra(NoteImageActivity.KEY_NOTE_ID, noteId);
                        activity.startActivity(viewImageIntent);
                        break;
                    case 1:
                        NotificationToast.showToast(activity, "Under construction...");
                        break;
                    default:
                        break;
                }
            }

        });

        return (alertDialog.create());
    }
}