package download.imageLoader.request;

import download.imageLoader.core.ImageLoader;
import download.imageLoader.core.LoadTask;
import download.imageLoader.listener.BackListener;
import download.imageLoader.util.DownloadBitmapUtils;
import download.imageLoader.util.ViewTaskUtil;
import download.imageLoader.view.GifMovieView;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapRequest{
	public WeakReference<View> view;
	public WeakReference<LoadTask> task;
	public BitmapDrawable percentBitmap;
	public BitmapDrawable bitmap;
	public Movie movie;
	public int width;
	public int height;
	public Boolean isFirstDown;
	public String path;
	public int percent;
	public long totalSize;
	public BackListener listener;

	public BitmapRequest(View view, String path, BackListener listener) {
		super();
		this.isFirstDown = false;
		this.view = new WeakReference<View>(view);
		this.path = path;
		this.listener = listener;
		this.task = new WeakReference<LoadTask>(null);
	}
	public BitmapRequest(View view, String path) {
		new BitmapRequest(view,path,null);
	}

	/**
	 * 是否是需要边下载边显示的图
	 * @return
	 */
	public Boolean isBigBitmap(){
		return totalSize > DownloadBitmapUtils.bigSize;
	}
	/**
	 * 检测是否需要异步获取
	 * @return
	 */
	public Boolean checkIfNeedAsyncLoad(){
		return isNoresult() || (bitmap == null && view.get() != null && !(view.get() instanceof GifMovieView)) ;
	}

	public Boolean isNoresult(){
		return (bitmap == null && movie == null);
	}

	/**
	 * 检测是否能用于显示
	 * @return
	 */
	public Boolean checkEffective(){
		if (this.view.get() != null && ((String)this.view.get().getTag()).equals(this.path)){
			return true;
		}
		return false;
	}


	public void display() {
		if (view.get() == null){
			return;
		}
		if (this != null && isFirstDown != null && isFirstDown && !isBigBitmap() && bitmap != null) {
			// 作为独立jar包不便用属性动画
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(
									Color.TRANSPARENT),
							bitmap });

			if (view.get() instanceof ImageView) {
				((ImageView) view.get())
						.setImageDrawable(td);
			} else {
				view.get().setBackgroundDrawable(td);
			}
			td.startTransition(500);
		} else {
			if (view.get() instanceof GifMovieView){
				if (movie != null){
					((GifMovieView) view.get()).setMovie(movie);
				}else {
					if (bitmap != null){
						((GifMovieView) view.get()).setImageDrawable(bitmap);
					}else {
						((GifMovieView) view.get()).setImageBitmap(ImageLoader.getInstance().getConfig().getFailedBm());
					}
				}
			}else{
				setBitmap(view.get(),bitmap);
			}
		}
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBitmap(View view,BitmapDrawable bitmap) {
		if (view == null) {
			return;
		}
		if (view instanceof ImageView) {
			((ImageView) view).setImageDrawable(bitmap);
		} else if (view instanceof ImageSwitcher) {
			((ImageSwitcher) view).setImageDrawable(bitmap);
		} else {// 对于其他自定义view
			view.setBackground(bitmap);
		}
	}

}
