package download.otherFileLoader.core;

import android.content.Context;

import download.otherFileLoader.request.DownBuilder;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class Download {
    public static String tempFileRootPath;

    public static DownBuilder with(Context context){
        return new DownBuilder(context);
    }
}
