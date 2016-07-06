package download.otherFileLoader.core;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.request.DownFile;

/**
 * Created by lizhiyun on 16/7/6.
 */
public class DownloadService extends Service {
    private ConnectionChangeReceiver connectionChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        connectionChangeReceiver = new ConnectionChangeReceiver();
        registerReceiver(connectionChangeReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionChangeReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        String path = intent.getStringExtra("path");
        DownFile downFile = new DownFile(url,path);
        DownFileManager.getInstance(this).addTask(downFile);
        return super.onStartCommand(intent, flags, startId);
    }
}
