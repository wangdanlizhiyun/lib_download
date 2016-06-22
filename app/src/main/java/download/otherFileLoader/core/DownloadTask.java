package download.otherFileLoader.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.litesuits.go.SmartExecutor;
import java.io.File;
import java.util.HashMap;
import download.otherFileLoader.request.DownFile;


public class DownloadTask implements DownloadThread.DownListener{
    public DownFile downFile;
    private final Handler mHandler;
    private final SmartExecutor mExecutor;
    public volatile boolean isPaused;
    public volatile boolean isCancelled;
    private ConnectRunnable mConnectThread;
    private DownloadThread[] mDownloadThreads;
    private int[] mDownloadStatus;
    private long mLastStamp;
    private File destFile;


    public DownloadTask(DownFile entry, Handler mHandler, SmartExecutor mExecutor) {
        this.downFile = entry;
        this.mHandler = mHandler;
        this.mExecutor = mExecutor;
        this.destFile = downFile.getDownloadFile();
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
                    if (downFile.listener != null) {
                        downFile.listener.error(message);
                    }
                }
            }));
        }else{
            startDownload();
        }
    }

    private void startDownload() {
        downFile.state = Constants.DOWNLOAD_STATE_DOWNLOADING;
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
        mDownloadStatus = new int[Constants.BLOB_COUNT];
        for (int i = 0; i < Constants.BLOB_COUNT; i++) {
            startPos = i * block + downFile.ranges.get(i);
            if (i == Constants.BLOB_COUNT - 1) {
                endPos = downFile.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(downFile.url,destFile, i, startPos, endPos, this);
                mDownloadStatus[i] = Constants.DOWNLOAD_STATE_DOWNLOADING;
                mExecutor.execute(mDownloadThreads[i]);
            }else {
                mDownloadStatus[i] = Constants.DOWNLOAD_STATE_FINISH;
            }
        }
    }

    private void startSingleDownload() {
        downFile.state = 1;
        mDownloadThreads = new DownloadThread[1];
        mDownloadStatus = new int[1];
        mDownloadStatus[0] = Constants.DOWNLOAD_STATE_DOWNLOADING;
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
        mDownloadStatus[index] = Constants.DOWNLOAD_STATE_FINISH;
        for (int i = 0; i < mDownloadStatus.length;i++){
            if (mDownloadStatus[i] != Constants.DOWNLOAD_STATE_FINISH){
                return;
            }
        }

        notifyUpdate(downFile,Constants.WHAT_FINISH);
    }

    @Override
    public void onDownloadError(int index, String message) {
        for (int i = 0; i < mDownloadStatus.length; i++) {
        if (mDownloadStatus[i] != Constants.DOWNLOAD_STATE_FINISH && mDownloadStatus[i] != Constants.DOWNLOAD_STATE_ERROR) {
            mDownloadThreads[i].cancelByError();
            return;
        }
    }
        Message msg = mHandler.obtainMessage();
        msg.what = Constants.WHAT_ERROR;
        Bundle bundle = new Bundle();
        bundle.putString("error",message);
        msg.setData(bundle);
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

}
