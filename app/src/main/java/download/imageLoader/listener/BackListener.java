package download.imageLoader.listener;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;


public interface BackListener {
	/**
	 * 下载进度，仅对于网络下载可保存本地的情况下调用
	 * @param percent
	 */
	public void onProcess(int percent);

	/**
	 * 运行在ui线程
	 * @param bitmap
	 * @param movie
	 */
	public void onSuccess(BitmapDrawable bitmap,Movie movie);

	/**
	 * 加载失败
	 */
	public void onFailed();
}
