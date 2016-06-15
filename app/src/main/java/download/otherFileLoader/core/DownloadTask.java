package download.otherFileLoader.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.litesuits.go.SmartExecutor;
import java.io.File;
import java.util.HashMap;
import download.otherFileLoader.request.DownFile;


public class DownloadTask implements DownloadThread.ProcessListener{
    private  DownFile downFile;
    private final Handler mHandler;
    private final SmartExecutor mExecutor;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private ConnectRunnable mConnectThread;
    private DownloadThread[] mDownloadThreads;
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

    public Object buildTask(DownFile downFile){
        return null;
    }
    public void start() {
        if (downFile.totalLength <= 0){
            mExecutor.execute(new ConnectRunnable(downFile.url, new ConnectRunnable.ConnectListener() {
                @Override
                public void onConnected(int totalLength, Boolean isSupport) {
                    downFile.downLength = totalLength;
                    downFile.isSuppurtRanger = isSupport;
                    startDownload();

                }

                @Override
                public void onError(String message) {
//                    listener.error(message);
                }
            }));
        }else{
            startDownload();
        }
    }

    private void startDownload() {
        Object object = buildTask(downFile);
        if (object instanceof Runnable) {
            downFile.state = 2;
//                        mExecutor.execute((DownloadTask) object);
            if (downFile.isSuppurtRanger) {
                startMultiDownload();
            } else {
                startSingleDownload();
            }
        } else {
//                        listener.error(((Exception) object).getMessage());
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
        for (int i = 0; i < Constants.BLOB_COUNT; i++) {
            startPos = i * block + downFile.ranges.get(i);
            if (i == Constants.BLOB_COUNT - 1) {
                endPos = downFile.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(downFile.url,destFile, i, startPos, endPos, this);
                mExecutor.execute(mDownloadThreads[i]);
            }

        }
    }

    private void startSingleDownload() {
        downFile.state = 1;
        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(downFile.url,destFile, 0, 0, 0, this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    private void notifyUpdate(DownFile entry, int what) {
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
        notifyUpdate(downFile, Constants.WHAT_PROCESS);
    }

    @Override
    public void onDownloadError(int index, String message) {
        Message msg = mHandler.obtainMessage();
        msg.what = Constants.WHAT_ERROR;
        Bundle bundle = new Bundle();
        bundle.putString("error",message);
        msg.setData(bundle);
        msg.obj = message;
        mHandler.sendMessage(msg);
    }
}
