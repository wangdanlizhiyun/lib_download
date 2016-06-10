package download.http.request;

import java.util.ArrayList;
import java.util.Map;

import download.http.entity.FileEntity;
import download.http.exception.AppException;
import download.http.listener.ICallback;
import download.http.listener.OnGlobalExceptionListener;

import static download.http.request.RequestBuilder.*;

/**
 * Created by lizhiyun on 16/6/3.
 */
public class Request {

    private Object returnObject;
    private String tag;
    private ICallback callback;
    private boolean enableProgressUpdate = false;
    private boolean enableProgressDownload = false;
    private int maxRetryTime = 0;
    private volatile boolean isCanceled = false;
    public String filePath;
    public ArrayList<FileEntity> fileEntities;

    private OnGlobalExceptionListener globalExceptionListener;


    public void checkIfCancelled() throws AppException {
        if (isCanceled){
            throw new AppException(AppException.ErrorType.CANCEL,"canceled");
        }
    }

    public void cancel() {
        isCanceled = true;
    }

    private RequestMethod method;

    private String url;
    private Map<String ,String> headers;
    private String content;

    public  Request(){
    }

    public Object getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ICallback getCallback() {
        return callback;
    }

    protected void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public boolean isEnableProgressDownload() {
        return enableProgressDownload;
    }

    public void setEnableProgressDownload(boolean enableProgressDownload) {
        this.enableProgressDownload = enableProgressDownload;
    }

    public boolean isEnableProgressUpdate() {
        return enableProgressUpdate;
    }

    protected void setEnableProgressUpdate(boolean enableProgressUpdate) {
        this.enableProgressUpdate = enableProgressUpdate;
    }

    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public OnGlobalExceptionListener getGlobalExceptionListener() {
        return globalExceptionListener;
    }

    protected void setGlobalExceptionListener(OnGlobalExceptionListener globalExceptionListener) {
        this.globalExceptionListener = globalExceptionListener;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ArrayList<FileEntity> getFileEntities() {
        return fileEntities;
    }

    public void setFileEntities(ArrayList<FileEntity> fileEntities) {
        this.fileEntities = fileEntities;
    }

    public RequestMethod getMethod() {
        return method;
    }

    protected void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    protected void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }
}
