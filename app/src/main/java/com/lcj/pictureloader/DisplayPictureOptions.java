package com.lcj.pictureloader;

/**
 * Created by aniu520 on 9/24/2015.
 */
public class DisplayPictureOptions {
    private final boolean resetViewBeforeLoading;
    private final boolean showStubPicDuringLoading;
    private final boolean cachePicInMemory;
    private final boolean cachePicOnDisc;

    public DisplayPictureOptions(boolean cachePicOnDisc, boolean cachePicInMemory, boolean showStubPicDuringLoading, boolean resetViewBeforeLoading) {
        this.cachePicOnDisc = cachePicOnDisc;
        this.cachePicInMemory = cachePicInMemory;
        this.showStubPicDuringLoading = showStubPicDuringLoading;
        this.resetViewBeforeLoading = resetViewBeforeLoading;
    }

    public boolean isResetViewBeforeLoading(){
        return resetViewBeforeLoading;
    }
    public boolean isShowStubPicDuringLoading(){
        return  showStubPicDuringLoading;
    }
    public boolean isCachePicInMemory(){
        return cachePicInMemory;
    }
    public boolean isCachePicOnDisc(){
        return cachePicOnDisc;
    }



    public static class Builder{
        private  boolean resetViewBeforeLoading = false;
        private  boolean showStubPicDuringLoading = false;
        private  boolean cachePicInMemory = false;
        private  boolean cachePicOnDisc = false;

        public Builder resetViewBeforeLoading(){
            resetViewBeforeLoading = true;
            return this;
        }
        public Builder showStubPicDuringLoading(){
            showStubPicDuringLoading = true;
            return this;
        }
        public Builder cachePicInMemory(){
            cachePicInMemory = true;
            return this;
        }
        public Builder cachePicOnDisc(){
            cachePicOnDisc = true;
            return this;
        }

        public DisplayPictureOptions build(){
            return new DisplayPictureOptions(cachePicOnDisc, cachePicInMemory,showStubPicDuringLoading,resetViewBeforeLoading);
        }
    }


    public static DisplayPictureOptions createForListView(){
        return new DisplayPictureOptions(true, true, true, true);
    }


    public static DisplayPictureOptions createForSingleLoad() {
        return new DisplayPictureOptions(false, false, false, false);
    }
}
