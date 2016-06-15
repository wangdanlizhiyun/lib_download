package download.imageLoader.core;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import download.imageLoader.loader.Load;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.GaussianBlur;

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
    }

    public void cancel(){
        mCancel.set(true);
    }

    @Override
    public void run() {
        if (mCancel.get()){
            return;
        }
        while (mImageLoader.getmRunningTasksManager().hasDoingTask(mRequest)) {
            try {
                if (mCancel.get()){
                    return;
                }
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
            Load.loadBitmap(mRequest, mImageLoader.getConfig());
            if (mRequest.checkIfNeedAsyncLoad()){
                mImageLoader.getConfig().cache.getDiskCacheBitmap(mRequest);
            }

        }
        if (mCancel.get()){
            return;
        }
        blur();
        if (mCancel.get()){
            return;
        }
        refreashBitmap();
        mImageLoader.getConfig().cache.addMemoryBitmap(mRequest);
        mImageLoader.getmRunningTasksManager().removeDoingTask(mRequest);
    }


    private void blur() {
        if (mRequest.isBlur && mRequest.movie == null && mRequest.bitmap != null) {
            mRequest.bitmap = new BitmapDrawable(new GaussianBlur().blur(mRequest.bitmap.getBitmap(), 3));
        }
    }


    private void refreashBitmap() {
        if (mCancel.get()){
            return;
        }
        //使用消息缓存
        Message message = Message.obtain();
        message.what = REFRESH;
        message.obj = mRequest;
        if (sUIHandler != null){
            sUIHandler.sendMessage(message);
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
                        if (request.view == null || request.view.get() == null){
                            return;
                        }
                        request.display();
                    break;

                default:
                    break;
            }

        }

    };

}