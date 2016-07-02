package download.otherFileLoader.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import download.otherFileLoader.request.DownFile;

/**
 * Created by lizhiyun on 16/7/1.
 */
public class DownService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            DownFile downFile = (DownFile) intent.getSerializableExtra("data");

        }
        return super.onStartCommand(intent, flags, startId);
    }
}
