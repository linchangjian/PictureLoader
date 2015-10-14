package com.lcj.pictureloader;

import android.graphics.Bitmap;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by aniu520 on 9/23/2015.
 */
public class PictureCache extends LimitedCache<String, Bitmap>{

    private int sizeLimit;

    public PictureCache(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Override
    protected Reference<Bitmap> createReference(Bitmap value) {
        return new WeakReference<Bitmap>(value);
    }

    @Override
    protected int getSizeLimit() {
        return sizeLimit;
    }

    @Override
    protected int getSize(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
