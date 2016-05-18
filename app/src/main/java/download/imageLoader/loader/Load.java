package download.imageLoader.loader;

import java.io.File;
import java.net.URI;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.DownloadBitmapUtils;
import android.graphics.Bitmap;

/**
 * 图片加载器
 * 
 * @author Administrator
 *
 */
public class Load {
	public static Bitmap loadBitmap(BitmapRequest request, ImageConfig config,
			BackListener listener) {
		Bitmap bm = null;
		UrlType urlType = download.imageLoader.util.UrlParser.getUrlType(request.path);
		switch (urlType) {
		case HTTP:
			DownloadBitmapUtils.downloadBitmapByUrl(request, config.cache.getmDiskLruCacheBitmap(), listener);
			bm = config.cache.getDiskCacheBitmap(request);
			if (bm != null) {
				return bm;
			}
			bm = DownloadBitmapUtils.downloadImgByUrl(request);
			break;
		case ASSETS:
			bm = DownloadBitmapUtils.loadImageFromAssets(request);
			break;
		case DRAWABLE:
			bm = DownloadBitmapUtils.loadImageFromDrawable(request);
			break;
		case FILE:
			bm = DownloadBitmapUtils.loadImageFromLocal(new File(URI.create(request.path))
					.getAbsolutePath().substring(1), request);
			break;
		case UNKNOWN:
			break;

		default:
			break;
		}
		return bm;
	}
}
