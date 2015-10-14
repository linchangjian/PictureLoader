package com.lcj.pictureloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Stack;

/**
 * Created by aniu520 on 9/23/2015.
 */
public class PictureLoader {
    public static final String TAG = PictureLoader.class.getSimpleName();

    private final PictureCache mPictureCache = new PictureCache(2000000);

    private final int MAX_PIC_DIMENSION_WIDTH;
    private final int MAX_PIC_DIMENSION_HEIGHT;

    private PhotoLoader photoLoaderThread = new PhotoLoader();

    private final File cacheDir;

    private PhotosQueue photosQueue = new PhotosQueue();
    private static PictureLoader instance = null;
    public static PictureLoader getInstance(Context context){
        if(instance == null){
            instance = new PictureLoader(context);
        }
        return instance;
    }

    private PictureLoader(Context context) {

        MAX_PIC_DIMENSION_HEIGHT = (int)(context.getResources().getDisplayMetrics().heightPixels);
        MAX_PIC_DIMENSION_WIDTH = (int)(context.getResources().getDisplayMetrics().widthPixels);

        photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
        this.cacheDir = StorageUtils.getCacheDirectory(context);

    }

    public void displayImage(String url, ImageView imageView, DisplayPictureOptions options, PictureLoadingListener listener) {
        imageView.setTag(url);
        if(url == null && url.length() == 0){
            Log.e(TAG,"displayImage url is null");
            return;
        }
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView, options, listener);
        synchronized (mPictureCache){
            if(mPictureCache.containsKey(url)){

                Object image = mPictureCache.get(url);
                if(image != null && !((Bitmap)image).isRecycled()){
                    imageView.setImageBitmap((Bitmap)image);
                }else{
                    queuePhoto(photoToLoad);
                    if(options.isShowStubPicDuringLoading()){
                        imageView.setImageResource(Constants.STUB_IMAGE);

                    }else{
                        if(options.isResetViewBeforeLoading()){
                            imageView.setImageBitmap(null);

                        }
                    }
                }

            }else{
                queuePhoto(photoToLoad);
                if(options.isShowStubPicDuringLoading()){
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    if(options.isResetViewBeforeLoading()){
                        imageView.setImageBitmap(null);
                    }
                }

            }
        }
    }

    private void queuePhoto(PhotoToLoad photoToLoad) {
        if(photoToLoad.listener != null){
            photoToLoad.listener.onLoadStated();
        }

        photosQueue.clean(photoToLoad.imageView);

        if(isCachedImage(photoToLoad.url)){
            synchronized (photosQueue.photosToLoadCached){
                photosQueue.photosToLoadCached.push(photoToLoad);
            }

        }else{
            synchronized (photosQueue.photosToLoad){
                photosQueue.photosToLoad.push(photoToLoad);
            }
        }

        synchronized (photosQueue.lock){
            photosQueue.lock.notifyAll();
        }

        if(photoLoaderThread.getState() == Thread.State.NEW){
            photoLoaderThread.start();
        }

    }

    private boolean isCachedImage(String url) {
        boolean result = false;
        File f = getLocalImageFile(url);
        try{
            result = f.exists();
        }catch (Exception e){

        }

        return result;
    }


    //队列任务
    private class PhotoToLoad{
        private String url;
        private ImageView imageView;
        private DisplayPictureOptions options;
        private PictureLoadingListener listener;

        public PhotoToLoad(String url, ImageView imageView, DisplayPictureOptions options, PictureLoadingListener listener) {
            this.url = url;
            this.imageView = imageView;
            this.options = options;
            this.listener = listener;
        }
    }

    class PhotosQueue{
        private Object lock = new Object();

        private final Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();
        private final Stack<PhotoToLoad> photosToLoadCached = new Stack<PhotoToLoad>();

        public void clean(ImageView image){
            for(int i = 0; i < photosToLoad.size();){
                if(photosToLoad.get(i).imageView == image){
                    photosToLoad.remove(i);
                }else{
                    ++i;
                }
            }


            for(int i = 0 ;i < photosToLoadCached.size();){
                if(photosToLoadCached.get(i).imageView == image){
                    photosToLoadCached.remove(i);
                }else{
                    ++i;
                }
            }
        }

    }


    private class PhotoLoader extends Thread{
        @Override
        public void run() {
            while(true){
                PhotoToLoad photoToLoad = null;
                Bitmap bmp = null;

                try{
                    if(photosQueue.photosToLoad.isEmpty() && photosQueue.photosToLoadCached.isEmpty()){
                        synchronized(photosQueue.lock){
                            photosQueue.lock.wait();
                        }
                    }
                    if(!photosQueue.photosToLoad.isEmpty()){
                       synchronized(photosQueue.photosToLoad){
                           photoToLoad = photosQueue.photosToLoad.pop();
                       }
                    }else if(!photosQueue.photosToLoadCached.isEmpty()){
                        synchronized(photosQueue.photosToLoadCached){
                            photoToLoad = photosQueue.photosToLoadCached.pop();
                        }
                    }

                    if(photoToLoad != null){
                        ImageSize targetImageSize = getImageSizeScaleTo(photoToLoad.imageView);
                        bmp = getBitmap(photoToLoad.url , targetImageSize , photoToLoad.options.isCachePicOnDisc());
                    }
                    if(bmp == null){
                        continue;
                    }
                    if(photoToLoad.options.isCachePicInMemory()){
                        synchronized(mPictureCache){
                            mPictureCache.put(photoToLoad.url, bmp);
                        }
                    }
                    if(Thread.interrupted()){
                        break;
                    }


                }catch(InterruptedException e){
                    Log.e(TAG, "" + e.getMessage());
                }finally {
                    if(photoToLoad != null){
                        PictureDisplayer pd = new PictureDisplayer(bmp, photoToLoad);
                        Activity a = (Activity)photoToLoad.imageView.getContext();
                        a.runOnUiThread(pd);
                    }
                }

            }

        }

    }

    /**
     * 用于显示bitmap在ui线程中
     */
    class PictureDisplayer implements Runnable{
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public PictureDisplayer(Bitmap bitmap, PhotoToLoad photoToLoad) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            String tag = (String)photoToLoad.imageView.getTag();
            if(photoToLoad != null && tag != null && tag.equals(photoToLoad.url) && bitmap != null){
                photoToLoad.imageView.setImageBitmap(bitmap);
                if(photoToLoad.listener != null){
                    photoToLoad.listener.onLoadComplete();

                }
            }

        }
    }

    private Bitmap getBitmap(String imageUrl, ImageSize targetImageSize, boolean cachePicOnDisc) {
        File file = getLocalImageFile(imageUrl);

        //尝试去加载sd卡中的图片
        try{
            if(file.exists()){
                Bitmap b = decodeFile(file, targetImageSize);
                if(b != null){
                    return b;
                }
            }
        }catch (IOException e){

        }
        //从网络上获取
        try{
            Bitmap bitmap = null;
            if(cachePicOnDisc){
                InputStream is = new URL(imageUrl).openStream();
                OutputStream os = new FileOutputStream(file);
                FileUtils.copyStream(is, os);
                is.close();
                os.close();
                bitmap = decodeFile(file, targetImageSize);

            }else{
                bitmap = decodeUrlFile(new URL(imageUrl), targetImageSize);
            }
            return bitmap;
        }catch(Exception e){
            Log.e(TAG, "Exception while loading bitmap from URL= "+ imageUrl +" : "+ e.getMessage(),e);
            return null;
        }


    }

    private Bitmap decodeUrlFile(URL imageUrl, ImageSize targetImageSize) throws IOException {
        InputStream is = imageUrl.openStream();
        BitmapFactory.Options decodeOptions = getBitmapOptionsForImageDecoding(is,targetImageSize);
        is.close();

        is = imageUrl.openStream();
        Bitmap result = decodeImageStream(is, decodeOptions);
        is.close();
        return result;
    }

    private Bitmap decodeImageStream(InputStream imageStream, BitmapFactory.Options decodeOptions) {
        Bitmap bitmap = null;
        try{
            bitmap = BitmapFactory.decodeStream(imageStream, null, decodeOptions);

        }catch(Throwable th){
            Log.e(TAG, "OUT OF MEMMORY : "+ th.getMessage());
        }
        return bitmap;
    }


    private Bitmap decodeFile(File imageFile, ImageSize targetImageSize)throws IOException {
        FileInputStream fis = new FileInputStream(imageFile);
        BitmapFactory.Options decodeOptions = getBitmapOptionsForImageDecoding(fis, targetImageSize);
        fis.close();

        fis = new FileInputStream(imageFile);
        Bitmap result = decodeImageStream(fis, decodeOptions);
        fis.close();

        return result;
    }

    private BitmapFactory.Options getBitmapOptionsForImageDecoding(InputStream imageStream, ImageSize targetImageSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeImageScale(imageStream, targetImageSize);
        return options;
    }

    private int computeImageScale(InputStream imageStream, ImageSize targetImageSize) {

        int width = targetImageSize.width;
        int height = targetImageSize.height;

        if(width < 0  && height < 0){
            width = MAX_PIC_DIMENSION_WIDTH;
            height = MAX_PIC_DIMENSION_HEIGHT;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(imageStream, null , options);

        int width_tmp = options.outWidth;
        int height_tmp = options.outHeight;

        int scale = 1;

        while(true){
            if(width_tmp / 2 < width || height_tmp / 2 < height){
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *=2;

        }
        return scale;
    }

    private File getLocalImageFile(String imageUrl) {
        String fileName = String.valueOf(imageUrl.hashCode());

        return new File(cacheDir,fileName);

    }

    private ImageSize getImageSizeScaleTo(ImageView imageView) {
        int width = -1;
        int height = -1;
        if (width < 0 && height < 0){
            ViewGroup.LayoutParams param =  imageView.getLayoutParams();
            width = param.width;
            height = param.height;
            if(width < 0 && height < 0){
                width = MAX_PIC_DIMENSION_WIDTH;
                height = MAX_PIC_DIMENSION_HEIGHT;

            }
        }
        return new ImageSize(width, height);
    }

    private class ImageSize {
        int width;
        int height;
        public ImageSize(int width, int height){
            this.width = width;
            this.height = height;
        }
    }
}
