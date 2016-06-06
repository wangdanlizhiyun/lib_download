package download.http.request;

import java.util.Map;

import download.http.exception.AppException;
import download.http.listener.ICallback;
import download.http.listener.OnGlobalExceptionListener;

/**
 * Created by lizhiyun on 16/6/3.
 */
public class Request {
    public Object returnObject;
    public  String tag;
    public ICallback callback;
    public boolean enableProgressUpdate = false;
    public int maxRetryTime = 3;
    public volatile boolean isCanceled = false;
    public void setCallback(ICallback callback){
        this.callback = callback;
    }

    public OnGlobalExceptionListener globalExceptionListener;
    public void setGlobalExceptionListener(OnGlobalExceptionListener globalExceptionListener) {
        this.globalExceptionListener = globalExceptionListener;
    }

    public void checkIfCanceled() throws AppException {
        if (isCanceled){
            throw new AppException(AppException.ErrorType.CANCEL,"canceled");
        }
    }

    public void cancel() {
        isCanceled = true;
    }

    public enum RequestMethod{GET,POST,PUT,DELETE}
    public RequestMethod method;

    public String url;
    public Map<String ,String> headers;
    public String content;

    public Request(String url,RequestMethod method){
        this.url = url;
        this.method = method;
    }
    public  Request(String url){
        this.url = url;
        this.method = RequestMethod.GET;
    }


}
