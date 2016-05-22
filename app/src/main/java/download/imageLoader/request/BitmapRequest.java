package download.imageLoader.request;

import download.imageLoader.listener.BackListener;
import download.imageLoader.view.GifMovieView;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.view.View;

public class BitmapRequest{
	public View view;
	public Bitmap bitmap;
	public Movie movie;
	public int width;
	public int height;
	public Boolean isAnimation;
	public String path;
	public int percent;
	public long totalSize;
	public BackListener listener;

	public BitmapRequest(View view, String path) {
		super();
		this.view = view;
		this.path = path;
	}
	public BitmapRequest(View view, String path, BackListener listener) {
		super();
		this.view = view;
		this.path = path;
		this.listener = listener;
	}
	public Boolean checkEmpty(){
		return (bitmap == null && movie == null) || (bitmap == null && !(view instanceof GifMovieView)) ;
	}
}
