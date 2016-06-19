package download.imageLoader.core;

import android.content.Context;

import download.imageLoader.request.BitmapRequestBuilder;

/**
 * Created by lizhiyun on 16/6/7.
 */
public class Image {
    public static BitmapRequestBuilder with(){
        return new BitmapRequestBuilder();
    }

}
