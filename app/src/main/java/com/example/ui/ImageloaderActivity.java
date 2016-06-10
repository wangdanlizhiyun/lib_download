package com.example.ui;

import java.util.ArrayList;
import java.util.List;


import download.http.core.Http;
import download.http.entity.ResultData;
import download.http.exception.AppException;
import download.http.exception.IfNeedLoginGlobalException;
import download.http.listener.JsonCallback;
import download.http.listener.JsonReaderCallback;
import download.imageLoader.config.FailedDrawable;
import download.imageLoader.core.BmManager;
import download.imageLoader.core.Image;
import download.imageLoader.listener.CustomDisplayMethod;
import download.imageLoader.view.PowerImageView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class ImageloaderActivity extends Activity implements OnScrollListener {
    private static final String TAG = "MainActivity";

    private List<String> mUrList = new ArrayList<String>();
    private GridView mImageGridView;
    private BaseAdapter mImageAdapter;

    private boolean mIsGridViewIdle = true;
    private int mImageWidth = 0;
    private boolean mIsWifi = false;
    private boolean mCanGetBitmapFromNetWork = false;
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imageloader);
        initData();
        initView();
        String url = "http://api.stay4it.com/v1/public/core/?service=user.login";
        String content = "account=stay4it&password=123456";
//        Http.with(this).url(url).post().content(content).downloadProcess()
//                .globalException(new IfNeedLoginGlobalException())
//                .callback(new JsonCallback<ResultData>() {
//
//                    @Override
//                    public ResultData onPost(ResultData resultData) {
//                        return super.onPost(resultData);
//                    }
//
//                    @Override
//                    public void onSuccess(ResultData result) {
//                        Log.e("test", "result=" + result == null ? "null" : result.data.toString());
//                    }
//                }).execute();
        Http.with(this).url(url).post().content(content).downloadProcess()
                .globalException(new IfNeedLoginGlobalException())
                .callback(new JsonReaderCallback<ResultData>() {

                    @Override
                    public void onSuccess(ResultData result) {
                        Log.e("test", "result=" + result == null ? "null" : result.data.toString());
                    }

                    @Override
                    public void onFailure(AppException exception) {
                        super.onFailure(exception);

                        Log.e("test", "exception="+exception.errorType + exception.getMessage());
                    }
                }).execute();


    }

    private void initData() {
        String[] imageUrls = {
                "http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg",
    			"http://img.my.csdn.net/uploads/201407/26/1406383290_1042.jpg",
    			"http://img.my.csdn.net/uploads/201407/26/1406383275_3977.jpg",
    			"http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg",
        		"http://img.blog.csdn.net/20160114230048304",//gif图
                "http://img.blog.csdn.net/20160114230048304",//gif图
                "http://img.blog.csdn.net/20160114230048304",//gif图
    			"assets://anim.gif",
                "assets://a.gif",
                "assets://e.gif",
                "assets://f.gif",
                "assets://c.gif",
                "assets://d.gif",
                "assets://d.gif",
                "assets://g.gif",
                "assets://h.gif",
                "drawable://"+R.drawable.anim,
                	"drawable://"+R.drawable.ic_launcher,
                	"file:///mnt/sdcard/paint.png",
        			"http://img.my.csdn.net/uploads/201407/26/1406383059_2237.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406383058_4330.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406383038_3602.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382942_3079.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382942_8125.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382942_4881.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382941_4559.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382941_3845.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382924_8955.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382923_2141.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382923_8437.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382922_6166.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382922_4843.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382905_5804.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382904_3362.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382904_2312.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382904_4960.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382900_2418.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382881_4490.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382881_5935.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382880_3865.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382880_4662.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382879_2553.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382862_5375.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382862_1748.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382861_7618.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382861_8606.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382861_8949.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382841_9821.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382840_6603.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382840_2405.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382840_6354.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382839_5779.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382810_7578.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382810_2436.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382809_3883.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382809_6269.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382808_4179.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382790_8326.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382789_7174.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382789_5170.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382789_4118.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382788_9532.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382767_3184.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382767_4772.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382766_4924.jpg",
        			"http://img.my.csdn.net/uploads/201407/26/1406382766_5762.jpg",
        };
        for (String url : imageUrls) {
            mUrList.add(url);
        }
        int screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        int space = (int)MyUtils.dp2px(this, 20f);
        mImageWidth = (screenWidth - space) / 3;
        mIsWifi = MyUtils.isWifi(this);
        if (mIsWifi) {
            mCanGetBitmapFromNetWork = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mImageGridView = (GridView) findViewById(R.id.gridView1);
        mImageAdapter = new ImageAdapter(this);
        mImageGridView.setAdapter(mImageAdapter);
        mImageGridView.setOnScrollListener(this);
        mImageGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
			}
		});

        if (!mIsWifi) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("初次使用会从网络下载大概5MB的图片，确认要下载吗？");
            builder.setTitle("注意");
            builder.setPositiveButton("是", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCanGetBitmapFromNetWork = true;
                    mImageAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("否", null);
            builder.show();
        }
        mTv = (TextView) findViewById(R.id.tv);

        Image.with(this).load("http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg")
                .size(130, 130).blur(false)
                .customDisplay(new CustomDisplayMethod() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void display(Drawable bitmap, Movie movie) {
                        mTv.setCompoundDrawablesRelativeWithIntrinsicBounds(bitmap, null, null, null);
                    }
                }).into(mTv);
        Button button = (Button) findViewById(R.id.button);
        button.setBackground(new FailedDrawable(Color.RED));
    }

    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Drawable mDefaultBitmapDrawable;

        private ImageAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mUrList.size();
        }

        @Override
        public String getItem(int position) {
            return mUrList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.image_list_item,parent, false);
                holder = new ViewHolder();
                holder.imageView = (PowerImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final PowerImageView imageView = holder.imageView;
            final String uri = getItem(position);

            imageView.setBackgroundColor(Color.RED);

            if (position == 0){
            imageView.circle().blur(false).setBorder(Color.BLACK, 0f).bind(uri);

            }else if (position == 1){
                imageView.rectangle().blur(false).setBorder(Color.BLUE, 15f).bind(uri);

            }else if (position == 2){
                imageView.round(50).blur(false).setBorder(Color.GREEN, 20f).bind(uri);

            }else if (position == 3){
                imageView.round(50).blur(true).setBorder(Color.GREEN, 20f).bind(uri);

            }else {
                imageView.round(50).blur(false).setBorder(Color.GREEN, 0f).bind(uri);

            }
//            imageView.setCircle().bind(uri);
//            imageView.setRectangle().bind(uri);
//            }
            convertView.getLayoutParams().width = mImageWidth;
            convertView.getLayoutParams().height = mImageWidth;
            return convertView;
        }

    }

    private static class ViewHolder {
        public PowerImageView imageView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState != OnScrollListener.SCROLL_STATE_FLING) {
            mIsGridViewIdle = true;
//            mImageAdapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // ignored
        
    }
    
}
