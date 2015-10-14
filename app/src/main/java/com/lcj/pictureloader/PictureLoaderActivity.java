package com.lcj.pictureloader;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PictureLoaderActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_loader);
        ListView listView = getListView();
        listView.setAdapter(new ItemAdapter());
        
        
    }

    class ItemAdapter extends BaseAdapter {

        private String[] pics = {
                "http://www.hrjt.com.cn/admin/admin_up/gcjc/20115131424000.jpg",
                "http://img4.imgtn.bdimg.com/it/u=3685097454,3631803668&fm=21&gp=0.jpg",
                "http://img5.imgtn.bdimg.com/it/u=1606258999,420145522&fm=21&gp=0.jpg",
                "http://img2.imgtn.bdimg.com/it/u=1628898140,3021734620&fm=21&gp=0.jpg",
                "http://www.hrjt.com.cn/admin/admin_up/gcjc/20115131424000.jpg",
                "http://img4.imgtn.bdimg.com/it/u=3685097454,3631803668&fm=21&gp=0.jpg",
                "http://img5.imgtn.bdimg.com/it/u=1606258999,420145522&fm=21&gp=0.jpg",
                "http://img2.imgtn.bdimg.com/it/u=1628898140,3021734620&fm=21&gp=0.jpg",
                "http://www.hrjt.com.cn/admin/admin_up/gcjc/20115131424000.jpg",
                "http://img4.imgtn.bdimg.com/it/u=3685097454,3631803668&fm=21&gp=0.jpg",
                "http://img5.imgtn.bdimg.com/it/u=1606258999,420145522&fm=21&gp=0.jpg",
                "http://img2.imgtn.bdimg.com/it/u=1628898140,3021734620&fm=21&gp=0.jpg",
                "http://www.hrjt.com.cn/admin/admin_up/gcjc/20115131424000.jpg",
                "http://img4.imgtn.bdimg.com/it/u=3685097454,3631803668&fm=21&gp=0.jpg",
                "http://img5.imgtn.bdimg.com/it/u=1606258999,420145522&fm=21&gp=0.jpg",
                "http://img2.imgtn.bdimg.com/it/u=1628898140,3021734620&fm=21&gp=0.jpg"
        };

        public PictureLoader pictureloader;

        public ItemAdapter( ) {
            this.pictureloader = PictureLoader.getInstance(PictureLoaderActivity.this);
        }

        @Override
        public int getCount() {
            return pics.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if(convertView == null){
                view = getLayoutInflater().inflate(R.layout.item, null);
                holder = new ViewHolder();
                holder.text = (TextView)view.findViewById(R.id.text);
                holder.image = (ImageView)view.findViewById(R.id.image);
                view.setTag(holder);
            }else{
                holder = (ViewHolder)view.getTag();
            }

            holder.text.setText("item "+position);
            DisplayPictureOptions options = new DisplayPictureOptions.Builder()
                    .resetViewBeforeLoading()
                    .cachePicInMemory()
                    .cachePicOnDisc()
                    .showStubPicDuringLoading()
                    .resetViewBeforeLoading()
                    .build();
            pictureloader.displayImage(pics[position], holder.image, options, new PictureLoadingListener(){


                @Override
                public void onLoadStated() {
                    holder.text.setText("...loading...");
                }

                @Override
                public void onLoadComplete() {
                    holder.text.setText("Item " + position);


                }
            });
            return view;
        }

        public class ViewHolder {
            public TextView text;
            public ImageView image;
        }
    }

}
