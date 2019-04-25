package org.octabyte.zeem.Utils;

import android.content.Context;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class ExoUtil {

    private static Cache cache;

    public static synchronized Cache getCache(Context context){
        if (cache == null){
            File cacheDirectory = new File(context.getExternalFilesDir(null), "cache");
            cache = new SimpleCache(cacheDirectory, new NoOpCacheEvictor());
        }

        return cache;
    }

}