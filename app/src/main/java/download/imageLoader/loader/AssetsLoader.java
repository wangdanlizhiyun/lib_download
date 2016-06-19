package download.imageLoader.loader;

import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;


import java.io.IOException;
import java.io.InputStream;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil;

/**
 * Created by lizhiyun on 16/6/2.
 */
public class AssetsLoader implements LoadInterface {

    @Override
    public void load(BitmapRequest request, ImageConfig config) {
        String name = request.path.substring(9);
        InputStream is = null;
        try {
            if (request.view == null || request.view.get() == null){
                return;
            }
            is = request.view.get().getContext().getAssets().open(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is == null) {
            return;
        }
        ImageSizeUtil.getImageViewSize(request);
        request.movie = Movie.decodeStream(is);
        if (request.checkIfNeedAsyncLoad()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
                    request.width, request.height);
            options.inJustDecodeBounds = false;
            request.bitmap = BitmapFactory.decodeStream(is, null, options);
        }
    }
}
