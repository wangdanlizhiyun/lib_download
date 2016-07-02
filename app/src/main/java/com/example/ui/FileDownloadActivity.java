package com.example.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import download.http.core.Http;
import download.http.listener.JsonReaderListCallback;
import download.http.listener.OnProgressDownloadListener;
import download.http.listener.OnProgressUpdateListener;
import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.Download;
import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.listener.DownloadListener;
import download.otherFileLoader.request.DownBuilder;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.ToastUtils;

public class FileDownloadActivity extends Activity implements View.OnClickListener{


    DownFileManager mDownloadManager;

    Button pauseall;


    private ListView mDownloadLsv;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filedownload);
        mDownloadManager = DownFileManager.getInstance(this);

        pauseall = (Button) findViewById(R.id.pauseall);
        pauseall.setOnClickListener(this);

        mDownloadLsv = (ListView) findViewById(R.id.mDownloadLsv);
        mDownloadLsv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        String url = "http://api.stay4it.com/v1/public/core/?service=downloader.applist";
        Http.with(this).url(url).callback(new JsonReaderListCallback<AppEntry>("data") {
            @Override
            public ArrayList<AppEntry> onPost(ArrayList<AppEntry> appEntries) {
                for (int i = 0; i < appEntries.size(); i++) {

                    DownFile downFile = DownFileManager.getInstance(getApplicationContext()).initData(new DownBuilder(FileDownloadActivity.this).url(appEntries.get(i).url).build());
                    appEntries.get(i).downLength = downFile.downLength;
                    appEntries.get(i).totalLength = downFile.totalLength;
                    appEntries.get(i).state = downFile.state;
                }
                return super.onPost(appEntries);
            }

            @Override
            public void onSuccess(ArrayList<AppEntry> result) {
                Log.e("test", "" + result.size());

                adapter = new DownloadAdapter(result);
                mDownloadLsv.setAdapter(adapter);

                for (final AppEntry entry:result
                     ) {
                    if (entry.state == DownFile.DownloadStatus.DOWNLOADING || entry.state == DownFile.DownloadStatus.WAITING){
                        Download.with(FileDownloadActivity.this).url(entry.url).listen(getDownloadListener(entry)).download();
                    }
                }
            }
        }).get();
    }
    Boolean isVisiable = false;

    @Override
    protected void onPause() {
        super.onPause();
        isVisiable = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisiable = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pauseall:
                if (pauseall.getText().equals("pauseall")){
                    pauseall.setText("recoverall");
                    mDownloadManager.pauseAll();
                }else {
                    pauseall.setText("pauseall");
                    mDownloadManager.recoverAll();
                }
                break;
        }

    }
    class DownloadAdapter extends BaseAdapter {

        public ArrayList<AppEntry> applist;
        public DownloadAdapter(ArrayList<AppEntry> list){
            this.applist = list;
        }

        private ViewHolder holder;

        @Override
        public int getCount() {
            return applist != null ? applist.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return applist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(FileDownloadActivity.this).inflate(R.layout.activity_applist_item, null);
                holder = new ViewHolder();
                holder.mDownloadBtn = (Button) convertView.findViewById(R.id.mDownloadBtn);
                holder.mDownloadLabel = (TextView) convertView.findViewById(R.id.mDownloadLabel);
                holder.mDownloadStatusLabel = (TextView) convertView.findViewById(R.id.mDownloadStatusLabel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final AppEntry app = applist.get(position);



            holder.mDownloadLabel.setText(app.name + "  " + app.size + "\n" + app.desc);

            holder.mDownloadStatusLabel.setText(app.state + "\n"
                    + Formatter.formatShortFileSize(getApplicationContext(), app.downLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), app.totalLength));
            holder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (app.state != DownFile.DownloadStatus.DOWNLOADING && app.state != DownFile.DownloadStatus.FINISH && app.state != DownFile.DownloadStatus.WAITING) {
                        Download.with(FileDownloadActivity.this).url(app.url).listen(getDownloadListener(app)).download();
                    } else if (app.state == DownFile.DownloadStatus.FINISH) {
                        //完成
                    } else if (app.state == DownFile.DownloadStatus.DOWNLOADING || app.state == DownFile.DownloadStatus.WAITING) {
                        Download.with(FileDownloadActivity.this).url(app.url).pause();
                    }
                }
            });
            return convertView;
        }
    }
    public DownloadListener getDownloadListener(final AppEntry entry){

        return new DownloadListener() {
            @Override
            public void success(String path) {
                entry.state = DownFile.DownloadStatus.FINISH;
                adapter.notifyDataSetChanged();
                ToastUtils.showToast(FileDownloadActivity.this,"已完成"+path);
            }

            @Override
            public void progress(int currentLen, int totalLen) {
                if (!isVisiable){
                    return;
                }
                entry.downLength = currentLen;
                entry.totalLength = totalLen;
                entry.state = DownFile.DownloadStatus.DOWNLOADING;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void error() {
                entry.state = DownFile.DownloadStatus.ERROR;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void waiting() {
                entry.state = DownFile.DownloadStatus.WAITING;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void pause() {
                entry.state = DownFile.DownloadStatus.PAUSE;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void cancel() {
                entry.state = DownFile.DownloadStatus.CANCEL;
                adapter.notifyDataSetChanged();
            }
        };
    }

    static class ViewHolder {
        TextView mDownloadLabel;
        TextView mDownloadStatusLabel;
        Button mDownloadBtn;
    }

}
