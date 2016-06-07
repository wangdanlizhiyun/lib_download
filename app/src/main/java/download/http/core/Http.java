package download.http.core;

import android.content.Context;

import download.http.request.RequestBuilder;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class Http {
    public static RequestBuilder with(Context context){
        return new RequestBuilder(context);
    }
}
