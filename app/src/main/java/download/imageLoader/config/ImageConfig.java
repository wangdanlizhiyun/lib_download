package download.imageLoader.config;

import java.io.InputStream;

import download.imageLoader.cache.BitmapCache;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

@SuppressLint("NewApi")
public class ImageConfig {
	public BitmapCache cache;
	private Drawable loadingBm = null;
	private Drawable failedBm;
	/**
	 *只在wifi下下载
	 */
	private Boolean onlyWifiMode = false;
	/**
	 * 仅使用内存缓存模式，针对图片名不变图片经常变动的商城项目这样的需求。
	 */
	private Boolean onlyMemoryMode = false;

	public void setOnlyWifiMode(Boolean b){
		this.onlyWifiMode = b;
	}

	public void setOnlyMemoryMode(Boolean b){
		this.onlyMemoryMode = b;
	}

	public Boolean getOnlyWifiMode(){
		return onlyWifiMode;
	}

	public Boolean getOnlyMemoryMode(){
		return onlyMemoryMode;
	}

	public void setFailedIdAndLoadingId(Resources res,int failedId,int loadingId) {
		ifIint = true;
		this.failedBm = new BitmapDrawable(res,BitmapFactory.decodeResource(res, failedId));
		this.loadingBm = new BitmapDrawable(res,BitmapFactory.decodeResource(res, loadingId));
	}


	private Boolean ifIint = false;
	public void initDefault(Context context){
		if (!ifIint){
			ifIint = true;
			loadingBm = new LoadingDrawable();
			failedBm = new FailedDrawable(Color.RED);
		}
	}
	public Drawable getLoadingBm() {
		return loadingBm;
	}

	public Drawable getFailedBm() {
		return failedBm;
	}


	public ImageConfig() {
		super();
		cache = new BitmapCache();
	}

}
