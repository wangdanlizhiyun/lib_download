package download.imageLoader.request;

import download.imageLoader.core.ImageLoader;
import download.imageLoader.core.LoadTask;
import download.imageLoader.listener.BackListener;
import download.imageLoader.listener.CustomDisplayMethod;
import download.imageLoader.view.PowerImageView;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapRequest{
	public static final int bigSize = 1024 * 1024;
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
	public CustomDisplayMethod customDisplayMethod;

	private BitmapRequest(){}
	public BitmapRequest(View view, String path,int width,int height, BackListener listener) {
		super();
		this.isFirstDown = false;
		this.view = new WeakReference<View>(view);
		this.path = path;
		this.listener = listener;
		this.width = width;
		this.height = height;
		this.task = new WeakReference<LoadTask>(null);
	}
	public BitmapRequest(View view, String path, BackListener listener) {
		super();
		this.isFirstDown = false;
		this.view = new WeakReference<View>(view);
		this.path = path;
		this.listener = listener;
		this.task = new WeakReference<LoadTask>(null);

	}
	public BitmapRequest(View view, String path) {
		super();
		this.isFirstDown = false;
		this.view = new WeakReference<View>(view);
		this.path = path;
		this.task = new WeakReference<LoadTask>(null);
	}
	public BitmapRequest(String path) {
		this.isFirstDown = false;
		this.view = new WeakReference<View>(null);
		this.path = path;
		this.task = new WeakReference<LoadTask>(null);
	}

	public BitmapRequest(View view, String path,int width,int height) {
		super();
		this.isFirstDown = false;
		this.view = new WeakReference<View>(view);
		this.path = path;
		this.width = width;
		this.height = height;
		this.task = new WeakReference<LoadTask>(null);
	}

	/**
	 * 是否是需要边下载边显示的图
	 * @return
	 */
	public Boolean isBigBitmap(){
		return totalSize > bigSize;
	}
	/**
	 * 检测是否需要异步获取
	 * @return
	 */
	public Boolean checkIfNeedAsyncLoad(){
		return isNoresult() || (bitmap == null && view.get() != null && !(view.get() instanceof PowerImageView)) ;
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
	public Boolean isNetWork() {
		return path.contains("http:") || path.contains("https:");
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void displayLoading(Drawable b) {
		if (view == null || view.get() == null){
			return;
		}
		if (customDisplayMethod != null){
			customDisplayMethod.display(b, movie);
			return;
		}
		if (view.get() instanceof PowerImageView){
			((PowerImageView) view.get()).setImageDrawable(b);
		}else{
			setBitmap(view.get(), bitmap);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void display() {
		Drawable drawable = bitmap != null ? bitmap : ImageLoader.getInstance().getConfig().getFailedBm();
		if (view == null || view.get() == null){
			return;
		}
		if (customDisplayMethod != null){
			customDisplayMethod.display(drawable,movie);
			return;
		}
		if (view.get() instanceof PowerImageView){
			if (movie != null){
				((PowerImageView) view.get()).setMovie(movie);
			}else {
				((PowerImageView) view.get()).setImageDrawable(drawable);
			}
		}else{
//			setBitmap(view.get(),bitmap != null ? bitmap : ImageLoader.getInstance().getConfig().getFailedBm());
			setBitmap(view.get(),bitmap);
			view.get().setBackgroundDrawable(ImageLoader.getInstance().getConfig().getFailedBm());

		}
		if (this != null && isFirstDown != null && isFirstDown && !isBigBitmap() && bitmap != null) {
			ObjectAnimator.ofFloat(view.get(),"alpha",0,1f).setDuration(500).start();
		}
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBitmap(View view,Drawable bitmap) {
		if (view == null) {
			return;
		}
		if (view instanceof ImageView) {
			((ImageView) view).setImageDrawable(bitmap);
		} else if (view instanceof ImageSwitcher) {
			((ImageSwitcher) view).setImageDrawable(bitmap);
		} else {// 对于其他view设置背景
			view.setBackground(bitmap);
		}
	}

}
