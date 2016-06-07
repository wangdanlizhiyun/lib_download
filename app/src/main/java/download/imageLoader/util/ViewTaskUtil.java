package download.imageLoader.util;

import android.view.View;
import com.litesuits.go.SmartExecutor;

import java.util.List;

import download.imageLoader.core.LoadTask;

/**
 * Created by lizhiyun on 16/5/23.
 */
public class ViewTaskUtil {
    /**
     * 移除旧任务
     * @param executor
     * @param view
     */
    public static void cancelOldTask(SmartExecutor executor,View view){
        try {
            List<Runnable> list = executor.getWaitingList();
            for (Runnable r : list) {
                if (r instanceof LoadTask){
                    LoadTask t = (LoadTask) r;
                    if (t.mRequest.view.get() != null && t.mRequest.view.get() == view){
                        t.cancel();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
