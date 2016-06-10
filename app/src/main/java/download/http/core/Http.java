package download.http.core;

import android.content.Context;

import download.http.request.RequestBuilder;
import download.utils.Util;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class Http {
    public static String tempFileRootPath;
    public static RequestBuilder with(Context context){
        if (tempFileRootPath == null){
            tempFileRootPath = Util.getDiskCacheDir(context,"temp").getAbsolutePath();
        }
        return new RequestBuilder(context);
    }
}
