package download.imageLoader.request;

import android.content.Context;
import android.view.View;

import java.lang.ref.WeakReference;
import download.imageLoader.core.ImageLoader;
import download.imageLoader.core.LoadTask;
import download.imageLoader.listener.CustomDisplayMethod;

/**
 * Created by lizhiyun on 16/6/3.
 */
public class BitmapRequestBuilder {
    public WeakReference<View> view;
    public int width;
    public int height;
    public String path;
    public CustomDisplayMethod customDisplayMethod;
    public Boolean isBlur;
    public Boolean isFace;

    public BitmapRequestBuilder(){
        this.isBlur = false;
        this.isFace = false;
    }

    public void into(View view){
        if (isBlur == null){
            isBlur = false;
        }
        BitmapRequest bitmapRequest = new BitmapRequest();
        bitmapRequest.path = path;
        bitmapRequest.width = width;
        bitmapRequest.height = height;
        bitmapRequest.customDisplayMethod = customDisplayMethod;
        bitmapRequest.view = new WeakReference<View>(view);
        bitmapRequest.isBlur = isBlur;
        bitmapRequest.isFace = isFace;
        ImageLoader.getInstance().loadImage(bitmapRequest);



    }
    public BitmapRequestBuilder blur(Boolean b){
        this.isBlur = b;
        return this;
    }
    public BitmapRequestBuilder face(Boolean b){
        this.isFace = b;
        return this;
    }

    public BitmapRequestBuilder size(int width,int height){
        this.width = width;
        this.height = height;
        return this;
    }
    public BitmapRequestBuilder load(String path){
        this.path = path;
        return this;
    }
    public BitmapRequestBuilder customDisplay(CustomDisplayMethod customDisplayMethod){
        this.customDisplayMethod = customDisplayMethod;
        return this;
    }



}
