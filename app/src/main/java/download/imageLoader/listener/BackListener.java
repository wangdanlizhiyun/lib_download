package download.imageLoader.listener;

import android.graphics.Bitmap;
import android.graphics.Movie;


public interface BackListener {
	/**
	 * 进度回调，运行在子线程
	 * @param percent
	 */
	public void onProcess(int percent);
	public void onSuccess(Bitmap bitmap,Movie movie);
}
