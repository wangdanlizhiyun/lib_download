package download.imageLoader.entity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.lang.ref.WeakReference;

import download.imageLoader.core.LoadTask;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class CustomDrawable extends BitmapDrawable {
    private WeakReference<LoadTask> mRunnable;
    private CustomDrawable(){}
    public CustomDrawable(Resources resources,Bitmap bitmap,LoadTask runnable){
        super(resources,bitmap);
        mRunnable = new WeakReference<LoadTask>(runnable);
    }
    public LoadTask getTask(){
        return mRunnable.get();
    }


}
