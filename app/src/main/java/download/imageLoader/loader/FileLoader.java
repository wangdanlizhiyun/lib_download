package download.imageLoader.loader;

import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;


import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil;

/**
 * Created by lizhiyun on 16/6/2.
 */
public class FileLoader implements download.imageLoader.loader.LoadInterface {
    @Override
    public void load(BitmapRequest request, ImageConfig config) {

        if (request.view == null || request.view.get() == null){
            return;
        }
        String path = new File(URI.create(request.path)).getAbsolutePath().substring(1);
        if (!new File(path).exists()) {
            return ;
        }
        ImageSizeUtil.getImageViewSize(request);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
                request.width, request.height);
        options.inJustDecodeBounds = false;

            try{
                request.movie = Movie.decodeStream(new FileInputStream(new File(path)));
            }catch (Exception e){

            }
            if (request.checkIfNeedAsyncLoad()){
                request.bitmap = new BitmapDrawable(BitmapFactory.decodeFile(path, options));
            }

    }
}
