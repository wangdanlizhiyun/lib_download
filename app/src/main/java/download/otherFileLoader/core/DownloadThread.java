package download.otherFileLoader.core;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import download.otherFileLoader.listener.DownloadListener;
import download.otherFileLoader.request.DownFile;

/**
 * Created by Stay on 4/8/15.
 * Powered by www.stay4it.com
 */
public class DownloadThread implements Runnable {
    private final String url;
    private final int startPos;
    private final int endPos;
    private final File destFile;
    private final DownListener listener;
    private final int index;
    private final boolean isSingleDownload;
    private volatile boolean isPaused;
    private volatile DownFile.DownloadStatus state = DownFile.DownloadStatus.IDLE;

    private volatile boolean isCancelled;
    private volatile boolean isError;

    public DownloadThread(String url, File destFile, int index, int startPos, int endPos, DownListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        this.destFile = destFile;
        if (startPos == 0 && endPos == 0) {
            isSingleDownload = true;
        } else {
            isSingleDownload = false;
        }

        this.listener = listener;
    }

    @Override
    public void run() {
        state = DownFile.DownloadStatus.DOWNLOADING;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            RandomAccessFile raf = null;
            BufferedOutputStream bos = null;
            BufferedInputStream is ;
            byte[] bytes;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(destFile, "rw");
                raf.seek(startPos);
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                bos = new BufferedOutputStream(new FileOutputStream(destFile));
            } else {
                state = DownFile.DownloadStatus.ERROR;
                listener.onDownloadError(index, "server error:" + responseCode);
                return;
            }
            String s;
            is = new BufferedInputStream(connection.getInputStream(),8 *1024);
            bytes = new byte[2048];
            int len;
            while ((len = is.read(bytes)) != -1) {
                if (isPaused || isCancelled || isError) {
                    break;
                }
                if (raf != null){
                    raf.write(bytes,0,len);
                }else {
                    bos.write(bytes,0,len);
                }
                listener.onProgressChanged(index, len);
            }
            if (raf != null)
                raf.close();
            if (bos != null)
                bos.close();
            is.close();

            if (!isPaused && !isCancelled && !isError){
                listener.onDownloadCompleted(index);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (!isPaused && !isCancelled && !isError){
                listener.onDownloadError(index, e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return state == DownFile.DownloadStatus.DOWNLOADING;
    }

    public void pause() {
        state = DownFile.DownloadStatus.PAUSE;
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public void cancel() {
        state = DownFile.DownloadStatus.CANCEL;
        isCancelled = true;
        Thread.currentThread().interrupt();
    }

    public void error() {
        state = DownFile.DownloadStatus.ERROR;
        isError = true;
        Thread.currentThread().interrupt();
    }
    interface DownListener {
        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);

    }
}
