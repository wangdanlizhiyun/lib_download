package download.http.listener;

/**
 * Created by lizhiyun on 16/6/4.
 */
public interface OnProgressDownloadListener {
    void onProgressDownload(int curLength, int totalLength);
}
