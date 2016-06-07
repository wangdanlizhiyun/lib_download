package download.http.listener;

import java.net.HttpURLConnection;

import download.http.exception.AppException;
import download.http.request.Request;

/**
 * Created by lizhiyun on 16/6/3.
 */
public interface ICallback<T> {
    void onProgressUpdate(int curLength, int totalLength);
    void onProgressDownload(int curLength, int totalLength);

    /**
     * 获取数据后的耗时预处理，在子线程
     * @param t
     * @return
     */
    T onPost(T t);
    void onSuccess(T result);
    void onFailure(AppException exception);
    void onCancel();
    T parse(Request request, HttpURLConnection connection, OnProgressListener listener) throws AppException;
}
