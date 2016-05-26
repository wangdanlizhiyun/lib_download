package download.imageLoader.listener;

import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;

/**
 * BackListener 的接口适配器，以便与更方便的使用
 */
public abstract class BackListenerAdapter implements BackListener{
	@Override
	public void onProcess(int percent) {

	}

	@Override
	public void onSuccess(BitmapDrawable bitmap, Movie movie) {

	}

	@Override
	public void onFailed() {

	}
}
