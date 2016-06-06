package download.http.request;

import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/5.
 */
public class IfNeedLoginBaseHandleRequest extends BaseHandleRequest {
    public IfNeedLoginBaseHandleRequest(String url) {
        super(url);
    }


    public IfNeedLoginBaseHandleRequest(String url, Request.RequestMethod method) {
        super(url, method);
    }

    @Override
    public boolean customHandleGlobaleException(AppException e) {

        return false;
    }
}
