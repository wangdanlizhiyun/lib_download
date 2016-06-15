package download.http.request;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

import download.http.core.HttpManager;
import download.http.entity.FileEntity;
import download.http.listener.ICallback;
import download.http.listener.OnGlobalExceptionListener;
import download.http.listener.OnProgressDownloadListener;
import download.http.listener.OnProgressUpdateListener;

/**
 * Created by lizhiyun on 16/6/3.
 */
public class RequestBuilder {
    private ICallback callback;
    public OnProgressUpdateListener onProgressUpdatedListener;
    public OnProgressDownloadListener onProgressDownloadListener;
    private int maxRetryTime = 3;
    private OnGlobalExceptionListener globalExceptionListener;
    public enum RequestMethod{GET,POST,PUT,DELETE}
    private RequestMethod method;
    public String filePath;
    public ArrayList<FileEntity> fileEntities;

    private String url;
    private Map<String ,String> headers;
    private String content;


    public RequestBuilder(Context context){
        this.method = RequestMethod.GET;
        this.maxRetryTime = 2;
    }
    public RequestBuilder url(String url){
        this.url = url;
        return this;
    }
    public RequestBuilder method(RequestMethod method){
        this.method = method;
        return this;
    }
    public RequestBuilder content(String content){
        this.content = content;
        return this;
    }
    public RequestBuilder headers(Map<String ,String> headers){
        this.headers = headers;
        return this;
    }

    public RequestBuilder maxRetryTime(int maxRetryTime){
        this.maxRetryTime = maxRetryTime;
        return this;
    }
    public RequestBuilder callback(ICallback callback){
        this.callback = callback;
        return this;
    }

    public RequestBuilder globalException(OnGlobalExceptionListener globalExceptionListener){
        this.globalExceptionListener = globalExceptionListener;
        return this;
    }


    public RequestBuilder post(){
        this.method = RequestMethod.POST;
        return this;
    }
    public RequestBuilder get(){
        this.method = RequestMethod.GET;
        return this;
    }
    public RequestBuilder delete(){
        this.method = RequestMethod.DELETE;
        return this;
    }
    public RequestBuilder put(){
        this.method = RequestMethod.PUT;
        return this;
    }

    public RequestBuilder progressUpdate(OnProgressUpdateListener listener){
        this.onProgressUpdatedListener = listener;
        return this;
    }
    public RequestBuilder progressDownload(OnProgressDownloadListener listener){
        this.onProgressDownloadListener = listener;
        return this;
    }
    public RequestBuilder filePath(String filePath){
        this.filePath = filePath;
        return this;
    }


    public RequestBuilder fileEntities(ArrayList<FileEntity> fileEntities){
        this.fileEntities = fileEntities;
        return this;
    }
    public void execute(){
        Request request = new Request();
        request.setUrl(url);
        request.setCallback(callback);
        request.setContent(content);
        request.setHeaders(headers);
        request.setMethod(method);
        request.setGlobalExceptionListener(globalExceptionListener);
        request.setMaxRetryTime(maxRetryTime);
        request.onProgressDownloadListener = onProgressDownloadListener;
        request.onProgressUpdatedListener = onProgressUpdatedListener;
        request.setFilePath(filePath);
        request.setFileEntities(fileEntities);
        HttpManager.getInstance().request(request);
    }

}
