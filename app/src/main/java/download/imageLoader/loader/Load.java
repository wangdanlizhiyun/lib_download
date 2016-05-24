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
	public static void loadBitmap(BitmapRequest request, ImageConfig config,
			BackListener listener) {
		UrlType urlType = download.imageLoader.util.UrlParser.getUrlType(request.path);
		switch (urlType) {
		case HTTP:
			request.isFirstDown = true;
			if (!config.getOnlyMemoryMode()){
				if (!config.getOnlyWifiMode()){
					DownloadBitmapUtils.downloadBitmapToDisk(request, config.cache.getmDiskLruCacheBitmap(), listener);
				}
				config.cache.getDiskCacheBitmap(request);
			}
			if (request.checkIfNeedAsyncLoad() && !config.getOnlyWifiMode()) {
				DownloadBitmapUtils.downloadImgByUrl(request);
			}
			break;
		case ASSETS:
			DownloadBitmapUtils.loadImageFromAssets(request);
			break;
		case DRAWABLE:
			DownloadBitmapUtils.loadImageFromDrawable(request);
			break;
		case FILE:
			DownloadBitmapUtils.loadImageFromLocal(new File(URI.create(request.path))
					.getAbsolutePath().substring(1), request);
			break;
		case UNKNOWN:
			break;

		default:
			break;
		}
	}
}
