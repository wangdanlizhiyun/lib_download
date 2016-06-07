package download.http.exception;

import download.http.listener.OnGlobalExceptionListener;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class IfNeedLoginGlobalException implements OnGlobalExceptionListener{
    @Override
    public boolean handleException(AppException e) {
        if (e.statusCode == 403){
            //TODO ...

            return true;
        }
        return false;
    }
}
