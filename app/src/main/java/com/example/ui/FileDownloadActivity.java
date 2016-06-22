package com.example.ui;

import android.app.Activity;
import android.os.Bundle;
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
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.ToastUtils;

public class FileDownloadActivity extends Activity implements View.OnClickListener{


    DownFileManager mDownloadManager;

    ArrayList<DownFile> mDownloadEntries = new ArrayList<DownFile>();
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
            public void onSuccess(ArrayList<AppEntry> result) {
                Log.e("test", "" + result.size());
                for (int i = 0; i < result.size(); i++) {
                    DownFile downFile = new DownFile(result.get(i).url);
                    downFile = DownFileManager.getInstance(getApplicationContext()).initData(downFile);
                    if (i == 1){
                        downFile.isInstall = true;
                    }
                    mDownloadEntries.add(downFile);
                }
                adapter = new DownloadAdapter(result);
                mDownloadLsv.setAdapter(adapter);
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
            final DownFile entry = mDownloadEntries.get(position);



            holder.mDownloadLabel.setText(app.name + "  " + app.size + "\n" + app.desc);

            holder.mDownloadStatusLabel.setText(entry.state + "\n"
                    + Formatter.formatShortFileSize(getApplicationContext(), entry.downLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));
            holder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entry.state != Constants.DOWNLOAD_STATE_DOWNLOADING && entry.state != Constants.DOWNLOAD_STATE_FINISH) {
                        entry.listener = new DownloadListener() {
                            @Override
                            public void success(String path) {
                                entry.state = Constants.DOWNLOAD_STATE_FINISH;
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
                                entry.state = Constants.DOWNLOAD_STATE_DOWNLOADING;
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void error(String errror) {
                                entry.state = Constants.DOWNLOAD_STATE_ERROR;
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void pause() {
                                entry.state = Constants.DOWNLOAD_STATE_PAUSE;
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void cancel() {
                                entry.state = Constants.DOWNLOAD_STATE_CANCEL;
                                adapter.notifyDataSetChanged();
                            }
                        };
                        mDownloadManager.down(entry);

                    } else if (entry.state == 1) {
                        //完成
                    } else if (entry.state == 2) {
                        mDownloadManager.pause(entry);
                    }
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView mDownloadLabel;
        TextView mDownloadStatusLabel;
        Button mDownloadBtn;
    }

}
