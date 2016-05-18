package download.imageLoader.config;

import java.io.InputStream;

import download.imageLoader.cache.BitmapCache;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
@SuppressLint("NewApi")
public class ImageConfig {
	public static ImageConfig mInstance;
	public BitmapCache cache;
	private Bitmap loadingBm = null,failedBm = null;


	public void setFailedIdAndLoadingId(Resources res,int failedId,int loadingId) {
		this.failedBm = BitmapFactory.decodeResource(res, failedId);
		this.loadingBm = BitmapFactory.decodeResource(res, loadingId);
	}


	public Bitmap getLoadingBm() {
		return loadingBm;
	}

	public Bitmap getFailedBm() {
		return failedBm;
	}


	public ImageConfig() {
		super();
		cache = new BitmapCache();
		loadingBm = getBitmapFormSrc("image/loading.png");
		failedBm = getBitmapFormSrc("image/loadfailed.png");
	}

	public static Bitmap getBitmapFormSrc(String src){
		Bitmap bit = null;
		try {
			InputStream is = ImageConfig.class.getResourceAsStream(src);
			bit = BitmapFactory.decodeStream(is);
	    } catch (Exception e) {
		}
		return bit;
	}
}
