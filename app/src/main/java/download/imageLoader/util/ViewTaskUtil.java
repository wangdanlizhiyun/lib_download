package download.imageLoader.util;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.litesuits.go.SmartExecutor;

import download.imageLoader.core.LoadTask;
import download.imageLoader.entity.CustomDrawable;
import download.imageLoader.request.BitmapRequest;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class ViewTaskUtil {

    public static LoadTask getTask(View view){
        if (view != null){
            LoadTask task = null;
            Drawable drawable = null;
            //如果是imageview就用drawable保存相互间的弱引用，否则保存在View的tag里
            if (view instanceof ImageView){
                drawable = ((ImageView) view).getDrawable();
            }else {
                drawable = (Drawable) view.getTag();
            }
            if (drawable instanceof CustomDrawable){
                CustomDrawable customDrawable = (CustomDrawable) drawable;
                return customDrawable.getTask();
            }
        }
        return null;
    }


    public static Boolean cancelOldTask(SmartExecutor executor,String url,View view){
        LoadTask task = getTask(view);
        if (task != null){
            if (task.mRequest.path != null && !task.mRequest.path.equals(url)){
                executor.cancelWaitingTask(task);
                Log.v("test", "cancelOldTask "+ url+ "  "+task.mRequest.path);
                task.cancel();
            }else {
                return false;
            }
        }
        return true;
    }

}
