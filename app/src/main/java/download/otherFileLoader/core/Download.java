package download.otherFileLoader.core;

import android.content.Context;

import download.http.request.RequestBuilder;
import download.otherFileLoader.request.Builder;
import download.utils.Util;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class Download {
    public static String tempFileRootPath;

    public static Builder with(Context context){
        return new Builder(context);
    }
}
