package download.imageLoader.loader;

import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil;

/**
 * Created by lizhiyun on 16/6/2.
 */
public class DrawableLoader implements LoadInterface {

    @Override
    public void load(BitmapRequest request, ImageConfig config) {
        if (request.view == null || request.view.get() == null){
            return;
        }
        int id = 0;
        try {
            id = Integer.parseInt(request.path.substring(11));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (id == 0) {
            return;
        }
        ImageSizeUtil.getImageViewSize(request);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(request.view.get().getResources(), id, options);
        options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
                request.width, request.height);
        options.inJustDecodeBounds = false;
        try{
            request.movie = Movie.decodeStream(request.view.get().getResources().openRawResource(id));
        }catch (Exception e){

        }
        if (request.checkIfNeedAsyncLoad()){
            request.bitmap = new BitmapDrawable(request.view.get().getResources(),BitmapFactory.decodeResource(request.view.get().getResources(), id, options));
        }
    }
}
