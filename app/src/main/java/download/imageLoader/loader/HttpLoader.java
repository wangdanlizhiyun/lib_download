package download.imageLoader.loader;

import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import download.imageLoader.cache.DiskLruCache;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil;
import download.utils.Util;

/**
 * Created by lizhiyun on 16/6/2.
 */
public class HttpLoader implements LoadInterface {

    @Override
    public void load(BitmapRequest request, ImageConfig config) {
        request.isFirstDown = true;
        if (!config.getOnlyMemoryMode()){
            if (!config.getOnlyWifiMode()){
                downloadBitmapToDisk(request, config.cache.getmDiskLruCacheBitmap());
            }
            config.cache.getDiskCacheBitmap(request);
        }
        if (request.checkIfNeedAsyncLoad() && !config.getOnlyWifiMode()) {
            downloadImgByUrl(request);
        }
    }
    public void downloadBitmapToDisk(BitmapRequest request,
                                            DiskLruCache diskLruCache) {
        if (diskLruCache == null) {
            return ;
        }
        final String key = Util.md5(request.path);
        DiskLruCache.Editor editor;
        try {
            editor = diskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                BufferedOutputStream out = null;
                BufferedInputStream in = null;
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(request.path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10*1000);
                    request.totalSize = conn.getContentLength();
                    in = new BufferedInputStream(conn.getInputStream(), 8 * 1024);
                    out = new BufferedOutputStream(outputStream, 8 * 1024);
                    int b = 0;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    editor.abort();
                } finally {
                    Util.close(conn, out, in);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void downloadImgByUrl(BitmapRequest request) {
        FileOutputStream fos = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(request.path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10*1000);
            is = new BufferedInputStream(conn.getInputStream());
            is.mark(is.available());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            ImageSizeUtil.getImageViewSize(request);
            options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
                    request.width, request.height);
            options.inJustDecodeBounds = false;
            request.movie = Movie.decodeStream(is);
            if (request.checkIfNeedAsyncLoad()){
                request.bitmap = BitmapFactory.decodeStream(is, null, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.close(conn, fos, is);
        }
    }
}
