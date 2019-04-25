package org.octabyte.zeem.Camera.camutil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import org.octabyte.zeem.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azeem on 6/19/2017.
 *         <p/>
 *         Singleton Class Used to Manage filters and process them all at once
 */
public final class ThumbnailsManager {
    private static List<ThumbnailItem> filterThumbs = new ArrayList<>(10);
    private static List<ThumbnailItem> processedThumbs = new ArrayList<>(10);

    private ThumbnailsManager() {
    }

    public static void addThumb(ThumbnailItem thumbnailItem) {
        filterThumbs.add(thumbnailItem);
    }

    public static List<ThumbnailItem> processThumbs(Context context) {
        for (ThumbnailItem thumb : filterThumbs) {
            // scaling down the image
            try {
                float size = context.getResources().getDimension(R.dimen.thumbnail_size);
                thumb.image = Bitmap.createScaledBitmap(thumb.image, (int) size, (int) size, false);
                thumb.image = thumb.filter.processFilter(thumb.image);
                //cropping circle
                //thumb.image = GeneralUtils.circularBitmap(thumb.image);
                processedThumbs.add(thumb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processedThumbs;
    }

    public static void clearThumbs() {
        filterThumbs = new ArrayList<>();
        processedThumbs = new ArrayList<>();
    }
}
