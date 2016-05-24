package download.imageLoader.listener;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;


public interface BackListener {
	/**
	 * 进度回调，运行在ui线程
	 * @param percent
	 */
	public void onProcess(int percent);

	/**
	 * 运行在ui线程
	 * @param bitmap
	 * @param movie
	 */
	public void onSuccess(BitmapDrawable bitmap,Movie movie);
}
