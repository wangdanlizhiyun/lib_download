package download.http.request;

import download.http.exception.AppException;
import download.http.listener.OnGlobalExceptionListener;

/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class BaseHandleRequest extends Request{


    public BaseHandleRequest(String url){
        super(url);
        initGlobalExceptionListener();
    }
    public BaseHandleRequest(String url, RequestMethod method){
        super(url,method);
        initGlobalExceptionListener();
    }

    private void initGlobalExceptionListener() {
        this.globalExceptionListener = new OnGlobalExceptionListener() {
            @Override
            public boolean handleException(AppException e) {
            if (e.statusCode == 403){
                return customHandleGlobaleException(e);
            }
            return false;
            }
        };
    }

    public abstract boolean customHandleGlobaleException(AppException e);
}
