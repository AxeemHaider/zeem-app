package org.octabyte.zeem.Camera.camutil;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;


/**
 * Created by Azeem on 6/19/2017.
 */
public class ThumbnailItem {
    public Bitmap image;
    public Filter filter;
    public String filterName;

    public ThumbnailItem() {
        image = null;
        filter = new Filter();
        filterName = "Filter";
    }
}
