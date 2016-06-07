package download.http.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.net.HttpURLConnection;

import download.http.exception.AppException;
import download.http.listener.OnProgressListener;
import download.http.request.Request;
import download.http.util.HttpUrlConnectionUtil;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class HttpTask implements  Runnable {
    public final static int UPDATEPROGRESS = 0;
    public final static int DOWNLOADPROGRESS = 1;
    public final static int RESULT = 2;
    public Request mRequest;
    int retryTime;
    private HttpTask(){}
    public HttpTask(Request request){
        super();
        this.mRequest = request;
    }

    @Override
    public void run() {
        notifyFinish(request(retryTime));
    }

    private Object request(int retryTime) {
        try {
            HttpURLConnection connection = HttpUrlConnectionUtil.execute(mRequest);
            if (mRequest.isEnableProgressUpdate()){
                return mRequest.getCallback().parse(mRequest, connection, new OnProgressListener() {
                    @Override
                    public void onProgressUpdate(int curLength, int totalLength) {
                        downloadProgress(curLength, totalLength);
                    }
                });
            }else {
                return mRequest.getCallback().parse(mRequest, connection, null);
            }
        } catch (AppException e) {
            if (e.errorType == AppException.ErrorType.IO){
                if (retryTime < mRequest.getMaxRetryTime()){
                    retryTime++;
                    return request(retryTime);
                }
            }
            return e;
        }
    }

    private void updateProgress(int curLength, int totalLength) {
        Message message = Message.obtain();
        message.what = UPDATEPROGRESS;
        message.obj = mRequest;
        message.arg1 = curLength;
        message.arg2 = totalLength;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
        }
    }
    private void downloadProgress(int curLength, int totalLength) {
        Message message = Message.obtain();
        message.what = DOWNLOADPROGRESS;
        message.obj = mRequest;
        message.arg1 = curLength;
        message.arg2 = totalLength;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
        }
    }

    private void notifyFinish(Object object) {
        Message message = Message.obtain();
        message.what = RESULT;
        mRequest.setReturnObject(object);
        message.obj = mRequest;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
        }
    }
    private static Handler sUIHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            final Request request = (Request) msg.obj;
            switch (msg.what) {
                case HttpTask.UPDATEPROGRESS:
                    request.getCallback().onProgressDownload(msg.arg1, msg.arg2);
                    break;
                case HttpTask.DOWNLOADPROGRESS:
                    request.getCallback().onProgressUpdate(msg.arg1, msg.arg2);
                    break;
                case HttpTask.RESULT:
                    if (request.getReturnObject() instanceof Exception){
                        if (request.isCanceled()){
                            request.getCallback().onCancel();
                            return;
                        }
                        if (request.getGlobalExceptionListener() != null){
                            if (!request.getGlobalExceptionListener().handleException((AppException) request.getReturnObject())){
                                request.getCallback().onFailure((AppException) request.getReturnObject());
                            }
                        }
                    }else {
                        request.getCallback().onSuccess(request.getReturnObject());
                    }
                    break;

                default:
                    break;
            }

        }

    };

}