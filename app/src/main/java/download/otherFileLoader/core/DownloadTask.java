package download.otherFileLoader.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.litesuits.go.SmartExecutor;
import java.io.File;
import java.util.HashMap;

import download.otherFileLoader.db.DLDBManager;
import download.otherFileLoader.request.DownFile;


public class DownloadTask implements DownloadThread.DownListener{
    public DownFile downFile;
    private final Handler mHandler;
    private final SmartExecutor mExecutor;
    public volatile boolean isPaused;
    public volatile boolean isCancelled;
    private ConnectRunnable mConnectThread;
    private DownloadThread[] mDownloadThreads;
    private DownFile.DownloadStatus[] mDownloadStatus;
    private long mLastStamp;
    private File destFile;
    DLDBManager dldbManager;


    public DownloadTask(DLDBManager dldbManager,DownFile entry, Handler mHandler, SmartExecutor mExecutor) {
        this.downFile = entry;
        this.mHandler = mHandler;
        this.mExecutor = mExecutor;
        this.destFile = new File(entry.downPath,entry.name);
        this.dldbManager = dldbManager;
    }

    public void pause() {
        isPaused = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    if (downFile.isSuppurtRanger) {
                        mDownloadThreads[i].pause();
                    } else {
                        mDownloadThreads[i].cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        isCancelled = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    mDownloadThreads[i].cancel();
                }
            }
        }
    }

    public void start() {
        if (downFile.totalLength == 0){
            mExecutor.execute(new ConnectRunnable(downFile.url, new ConnectRunnable.ConnectListener() {
                @Override
                public void onConnected(int totalLength, Boolean isSupport) {
                    downFile.totalLength = totalLength;
                    downFile.isSuppurtRanger = isSupport;
                    if (downFile.totalLength <= 0){
                        downFile.isSuppurtRanger = false;
                    }
                    startDownload();

                }

                @Override
                public void onError(String message) {
                    notifyUpdate(downFile, Constants.WHAT_ERROR);
                }
            }));
        }else{
            startDownload();
        }
    }


    private void startDownload() {
        downFile.state = DownFile.DownloadStatus.DOWNLOADING;
        if (downFile.isSuppurtRanger) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startMultiDownload() {
        int block = downFile.totalLength / Constants.BLOB_COUNT;
        int startPos = 0;
        int endPos = 0;
        if (downFile.ranges == null) {
            downFile.ranges = new HashMap<Integer, Integer>();
            for (int i = 0; i < Constants.BLOB_COUNT; i++) {
                downFile.ranges.put(i, 0);
            }
        }
        mDownloadThreads = new DownloadThread[Constants.BLOB_COUNT];
        mDownloadStatus = new DownFile.DownloadStatus[Constants.BLOB_COUNT];
        for (int i = 0; i < Constants.BLOB_COUNT; i++) {
            startPos = i * block + downFile.ranges.get(i);
            if (i == Constants.BLOB_COUNT - 1) {
                endPos = downFile.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(downFile.url,destFile, i, startPos, endPos, this);
                mDownloadStatus[i] = DownFile.DownloadStatus.DOWNLOADING;
                mExecutor.execute(mDownloadThreads[i]);
            }else {
                mDownloadStatus[i] = DownFile.DownloadStatus.FINISH;
            }
        }
    }

    private void startSingleDownload() {
        mDownloadThreads = new DownloadThread[1];
        mDownloadStatus = new DownFile.DownloadStatus[1];
        mDownloadStatus[0] = DownFile.DownloadStatus.DOWNLOADING;
        mDownloadThreads[0] = new DownloadThread(downFile.url,destFile, 0, 0, 0, this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    private void notifyUpdate(DownFile entry, int what) {
        if (what == Constants.WHAT_DOWNLOADING){
            if (System.currentTimeMillis() - downFile.lastNotifyTime > 1000 || downFile.downLength >= downFile.totalLength){
                downFile.lastNotifyTime = System.currentTimeMillis();
            }else {
                return;
            }
        }
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = entry;
        mHandler.sendMessage(msg);
        if (what == Constants.WHAT_ERROR){
            dldbManager.deleteTaskInfo(downFile);
        }else {
            dldbManager.insertOrUpdate(downFile);
        }
    }

    @Override
    public void onProgressChanged(int index, int progress) {
        if (downFile.isSuppurtRanger) {
            int range = downFile.ranges.get(index) + progress;
            downFile.ranges.put(index, range);
        }
        downFile.downLength += progress;
        notifyUpdate(downFile, Constants.WHAT_DOWNLOADING);
    }

    @Override
    public void onDownloadCompleted(int index) {
        Log.e("test","onDownloadCompleted  "+index);
        mDownloadStatus[index] = DownFile.DownloadStatus.FINISH;
        for (int i = 0; i < mDownloadStatus.length;i++){
            if (mDownloadStatus[i] != DownFile.DownloadStatus.FINISH){
                return;
            }
        }
        downFile.state = DownFile.DownloadStatus.FINISH;
        Log.v("test","finish"+downFile.state.name+downFile.downLength+" "+downFile.totalLength);
        notifyUpdate(downFile, Constants.WHAT_FINISH);
    }

    @Override
    public void onDownloadError(int index, String message) {
    for (int i = 0; i < mDownloadStatus.length; i++) {
        if (mDownloadStatus[i] != DownFile.DownloadStatus.FINISH && mDownloadStatus[i] != DownFile.DownloadStatus.ERROR) {
            mDownloadThreads[i].cancel();
        }
    }
        downFile.state = DownFile.DownloadStatus.ERROR;
        notifyUpdate(downFile, Constants.WHAT_ERROR);
    }

}
