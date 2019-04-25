package org.octabyte.zeem.Camera.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import static org.octabyte.zeem.Camera.CameraActivity.MEDIA_TYPE_IMAGE;
import static org.octabyte.zeem.Camera.CameraActivity.MEDIA_TYPE_VIDEO;
import static org.octabyte.zeem.Camera.CameraActivity.TAG;

/**
 * Created by Azeem on 6/23/2017.
 */

public final class GeneralUtils {

    /**
     * Returns Bitmaps with a circular shape.
     * @param input source Bitmap that need to convert in circular shape
     * @return Bitmaps in circular shape
     */
    public static Bitmap circularBitmap(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW
        );

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(input, 0, 0, null);
        return outputBitmap;
    }

    /**
     * Returns Identifier of String into it's ID as defined in R.java file.
     * @param pContext
     * @param pString defined in drawable resource name e.g: action_item_help
     * @return int Resource id e.g: R.drawable.smile
     */
    public static int getDrawableIdentifier(Context pContext, String pString){
        return pContext.getResources().getIdentifier(pString, "drawable", pContext.getPackageName());
    }


    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ZEEM");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Long timeStamp = System.currentTimeMillis();
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else{
            return null;
        }

        return mediaFile;
    }

    private GeneralUtils() {}

}
