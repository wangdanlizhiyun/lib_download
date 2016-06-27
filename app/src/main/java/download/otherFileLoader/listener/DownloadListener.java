package download.otherFileLoader.listener;

import download.otherFileLoader.request.DownFile;

/**
 * Created by lizhiyun on 16/6/15.
 */
public interface DownloadListener {
    public long lastTime = 0;
    void success(String path);
    void waiting();
    void progress(int currentLen,int totalLen);
    void error(String errror);
    void pause();
    void cancel();
}
