package download.otherFileLoader.core;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.listener.DownloadListener;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.PackageUtil;
import download.otherFileLoader.util.ToastUtils;


/**
 * Created by lizhiyun on 16/6/12.
 */
public class DownloadService extends Service {
    private SmartExecutor executor;
    private ArrayList<DownFile> downingFiles = new ArrayList<DownFile>();

    private Handler sHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            DownFile downFile = (DownFile) msg.obj;
            if (downFile == null) {
                return;
            }
            switch (msg.what){
                case Constants.WHAT_DOWNLOADING:
                    for (DownFile df:downingFiles
                            ) {
                        if (df.equals(downFile)) {
                            if (df.listener != null) {
                                df = downFile;
                                df.listener.progress(df.downLength,df.totalLength);
                            }
                        }
                    }
                    break;
                case Constants.WHAT_FINISH:
                    if (downFile.isAutoInstall) {
                        Boolean code = PackageUtil.install(getApplicationContext(), new File(downFile.downPath));
                        if (!code) {
                            ToastUtils.showToast(getApplicationContext(), "安装失败");
                        }
                    }
                    for (DownFile df:downingFiles
                        ) {
                    if (df.equals(downFile)){
                        if (df.listener != null){
                            df = downFile;
                            df.listener.success(df.downPath);
                        }
                    }
                }
                    break;
                case Constants.WHAT_PROCESS:
                    for (DownFile df:downingFiles
                         ) {
                        if (df.equals(downFile)){
                            if (df.listener != null){
                                df.listener.progress(downFile.downLength,downFile.totalLength);
                            }
                        }
                    }
                    break;
                case Constants.WHAT_ERROR:
                    for (DownFile df:downingFiles
                            ) {
                        if (df.equals(downFile)){
                            if (df.listener != null){
                                Bundle b = msg.getData();
                                df.listener.error(b.getString("error"));
                            }
                        }
                    }
                    break;
            }
            DownFileManager.getInstance(getApplicationContext()).save(downFile);

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        executor = new SmartExecutor(4, 100);
        executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
        executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        DownFile entry = null;
        try {
            entry = (DownFile) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            doAction(action,entry);

        }catch (Exception e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownFile entry) {
        switch (action){
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                startDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSEALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVERALL:
                recoverAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCELALL:
                cancelAll();
                break;

        }
    }

    private void recoverAll() {
    }
    private void cancelAll() {
    }

    private void pauseAll() {
    }

    private void resumeDownload(DownFile entry) {
        startDownload(entry);
    }

    private void pauseDownload(DownFile downFile) {
        for (DownFile df:downingFiles
                ) {
            if (df.equals(downFile)){
                df.isPaused = true;
            }
        }
    }

    private void cancelDownload(DownFile downFile) {
        for (DownFile df:downingFiles
                ) {
            if (df.equals(downFile)){
                df.isCanceled = true;
            }
        }
    }

    private Boolean hasIntasks(DownFile downFile){
        for (DownFile df:downingFiles
             ) {
            if (df.equals(downFile)){
                return true;
            }
        }
        return false;
    }
    private void startDownload(DownFile downFile) {
        Boolean exist = hasIntasks(downFile);
        DownFileManager.getInstance(getApplicationContext()).initData(downFile);
        downingFiles.add(downFile);
        if (!exist){
            new DownloadTask(downFile,sHandler,executor).start();
        }
    }


}
