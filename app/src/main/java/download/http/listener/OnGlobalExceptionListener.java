package download.http.listener;

import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/5.
 */
public interface OnGlobalExceptionListener {
    boolean handleException(AppException e);
}
