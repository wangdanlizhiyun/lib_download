package download.imageLoader.core;

import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import download.imageLoader.listener.BackListener;
import download.imageLoader.listener.BackListenerAdapter;
import download.imageLoader.loader.Load;
import download.imageLoader.loader.UrlType;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.DownloadBitmapUtils;
import download.imageLoader.util.UrlParser;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class LoadTask implements  Runnable {
    public final static int REFRESH = 1;
    public BitmapRequest mRequest;
    ImageLoader mImageLoader;
    private final AtomicBoolean mCancel = new AtomicBoolean();
    private LoadTask(){}
    public LoadTask(BitmapRequest request,ImageLoader loader){
        super();
        this.mRequest = request;
        this.mImageLoader = loader;
        this.mCancel.set(false);
        this.mRequest.task = new WeakReference<LoadTask>(this);
    }

    public void cancel(){
        mCancel.set(true);
    }
    @Override
    public void run() {
        while (mImageLoader.getmRunningTasksManager().hasDoingTask(mRequest)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mImageLoader.getmRunningTasksManager().addDoingTask(mRequest);

        mImageLoader.getConfig().cache.getMemoryCache(mRequest);
        if (mRequest.checkIfNeedAsyncLoad()){
            mImageLoader.getConfig().cache.getDiskCacheBitmap(mRequest);
        }
        if (mRequest.checkIfNeedAsyncLoad()) {
            Load.loadBitmap(mRequest, mImageLoader.getConfig(), new BackListenerAdapter() {

                @Override
                public void onProcess(int percent) {
                    if (mCancel.get()){
                        return;
                    }
                    if (mRequest.view == null) {
                        return;
                    }
                    if (mRequest.isBigBitmap()) {
                        if (percent % 10 != 0) {
                            return;
                        }
                        mImageLoader.getConfig().cache.getDiskCacheBitmap(mRequest);
                        mImageLoader.getConfig().cache.addPercentBitmap(mRequest.path,
                                mRequest.percentBitmap);
                        refreashBitmap();
                    }

                }
            });
            if (mRequest.checkIfNeedAsyncLoad()){
                mImageLoader.getConfig().cache.getDiskCacheBitmap(mRequest);
            }

        }
        refreashBitmap();
        mImageLoader.getConfig().cache.addMemoryBitmap(mRequest);
        mImageLoader.getmRunningTasksManager().removeDoingTask(mRequest);
    }



    private void refreashBitmap() {
        if (mCancel.get()){
            return;
        }
        Message message = Message.obtain();
        message.what = REFRESH;
        message.obj = mRequest;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
        }else {
            Log.v("test","sUIHandler == null");

        }
    }
    private static Handler sUIHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            final BitmapRequest request = (BitmapRequest) msg.obj;
            switch (msg.what) {
                case LoadTask.REFRESH:
                        if (!request.checkEffective()) {
                            return;
                        }
                        if (request.view.get() == null){
                            return;
                        }
                        if (request.listener == null) {
                            request.display();
                        } else {
                            // 设置了回调监听的自己处理
                            if (request.percent > 0){
                                request.listener.onProcess(request.percent);
                            }
                            if (request.isNoresult()){
                                request.listener.onFailed();
                            }else {
                                request.listener.onSuccess(request.bitmap,request.movie);
                            }
                        }

                    break;

                default:
                    break;
            }

        }

    };

}