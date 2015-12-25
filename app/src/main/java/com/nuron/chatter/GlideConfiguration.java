package com.nuron.chatter;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by nuron on 20/11/15.
 */
public class GlideConfiguration implements GlideModule {


    private static final int GLIDE_DISK_CACHE_SIZE = 40 * 1024 * 1024;  // 40 Mb
    private static final int GLIDE_LRU_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 3);

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, GLIDE_DISK_CACHE_SIZE));
        builder.setMemoryCache(new LruResourceCache(GLIDE_LRU_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // register ModelLoaders here.
    }
}