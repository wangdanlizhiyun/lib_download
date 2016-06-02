package download.imageLoader.loader;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;

/**
 * Created by lizhiyun on 16/6/2.
 */
public interface LoadInterface {
    public abstract void load(BitmapRequest request, ImageConfig config,
                              BackListener listener);
}
