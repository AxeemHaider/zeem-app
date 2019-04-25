package org.octabyte.zeem.Camera.camutil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.Log;

import static org.octabyte.zeem.Camera.CameraActivity.TAG;

/**
 * Created by Azeem on 6/19/2017.
 */

public class CameraUtils {

    /**
     * Creates a {@link Bitmap} from raw image byte data. This will usually come
     * from a {@link android.hardware.Camera.PictureCallback} returning byte data from the {@link
     * android.hardware.Camera}.
     *
     * @param data   Image data as a byte array.
     * @param width  Requested width for the created {@link Bitmap}.
     * @param height Requested height for the created {@link Bitmap}.
     * @param isFrontCam It is front camera or not
     *
     * @return A {@link Bitmap} containing the given image data best fit to a given
     * width and height.
     */
    public static Bitmap originalBmp;
    public static int originalBmpWidth, originalBmpHeight;

    public static Bitmap bitmapFromRawBytes(byte[] data, int width, int height, Boolean isFrontCam) {
        /*  Decode the dimensions of the bitmap data so we can determine a sample size that fits the
        requested width and height. */
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        if(isFrontCam)
            matrix.preScale(-1.0f, 1.0f);

        bitmap = Bitmap.createBitmap (bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //bitmap = resizeBitmap(bitmap, width + 200, height + 200);
            //Log.i(TAG, "width: "+bitmap.getWidth() +" height: "+bitmap.getHeight());

        originalBmp = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        originalBmpWidth = width;
        originalBmpHeight = height;
        Log.i(TAG, "originalBmp width: "+originalBmp.getWidth() +" height: "+originalBmp.getHeight());
        return originalBmp;
    }

    private CameraUtils(){}

}
