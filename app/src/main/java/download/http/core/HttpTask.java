package download.http.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

import download.http.exception.AppException;
import download.http.listener.OnProgressListener;
import download.http.request.Request;
import download.http.util.HttpUrlConnectionUtil;
import download.imageLoader.core.ImageLoader;
import download.imageLoader.listener.BackListenerAdapter;
import download.imageLoader.loader.Load;
import download.imageLoader.request.BitmapRequest;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class HttpTask implements  Runnable {
    public final static int PROGRESS = 1;
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
            if (mRequest.enableProgressUpdate){
                return mRequest.callback.parse(mRequest,connection, new OnProgressListener() {
                    @Override
                    public void onProgressUpdate(int curLength, int totalLength) {
                        publishProgress(curLength,totalLength);
                    }
                });
            }else {
                return mRequest.callback.parse(mRequest,connection,null);
            }
        } catch (AppException e) {
            if (e.errorType == AppException.ErrorType.IO){
                if (retryTime < mRequest.maxRetryTime){
                    retryTime++;
                    return request(retryTime);
                }
            }
            return e;
        }
    }

    private void publishProgress(int curLength, int totalLength) {
        Message message = Message.obtain();
        message.what = PROGRESS;
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
        mRequest.returnObject = object;
        message.obj = mRequest;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
        }
    }
    private static Handler sUIHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            final Request request = (Request) msg.obj;
            switch (msg.what) {
                case HttpTask.PROGRESS:
                    request.callback.onProgressUpdate(msg.arg1,msg.arg2);
                    break;
                case HttpTask.RESULT:
                    if (request.returnObject instanceof Exception){
                        if (request.isCanceled){
                            request.callback.onCancel();
                            return;
                        }
                        if (request.globalExceptionListener != null){
                            if (!request.globalExceptionListener.handleException((AppException) request.returnObject)){
                                request.callback.onFailure((AppException) request.returnObject);
                            }
                        }
                    }else {
                        request.callback.onSuccess(request.returnObject);
                    }
                    break;

                default:
                    break;
            }

        }

    };

}