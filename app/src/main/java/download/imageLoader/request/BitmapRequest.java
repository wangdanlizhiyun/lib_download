package download.imageLoader.request;

import download.imageLoader.core.ImageLoader;
import download.imageLoader.core.LoadTask;
import download.imageLoader.listener.CustomDisplayMethod;
import download.imageLoader.view.PowerImageView;
import download.utils.Util;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapRequest{
	public WeakReference<View> view;
	public Bitmap bitmap;
	public Movie movie;
	public int width;
	public int height;
	public Boolean isFirstDown;
	public Boolean isBlur;
	public Boolean isFace;
	public String path;
	public long totalSize;
	public CustomDisplayMethod customDisplayMethod;


	public BitmapRequest(){
		isFirstDown = false;
		isBlur = false;
	}
	public String getKey(){
		return Util.md5(this.path) + this.isBlur + this.isFace;
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
		if (this.view.get() != null && ((String)this.view.get().getTag()).equals(getKey())){
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
			customDisplayMethod.display(bitmap, movie);
			return;
		}
		if (view.get() instanceof PowerImageView){
			((PowerImageView) view.get()).setImageDrawable(b);
		}else{
			setBitmap(view.get(), b);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void display() {
		if (view == null || view.get() == null){
			return;
		}
		if (customDisplayMethod != null){
			customDisplayMethod.display(bitmap,movie);
			return;
		}
		if (view.get() instanceof PowerImageView){
			if (movie != null){
				((PowerImageView) view.get()).setMovie(movie);
			}else {
				if (bitmap != null){
					((PowerImageView) view.get()).setImageBitmap(bitmap);
				}else {
					((PowerImageView) view.get()).setImageDrawable(ImageLoader.getInstance().getConfig().getFailedBm());
				}
			}
		}else{
			if (bitmap != null){
				setBitmap(view.get(),bitmap);
			}else {
				setBitmap(view.get(),ImageLoader.getInstance().getConfig().getFailedBm());
			}
		}
		if (this != null && isFirstDown != null && isFirstDown && bitmap != null) {
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
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBitmap(View view,Bitmap bitmap) {
		if (view == null) {
			return;
		}
		if (view instanceof ImageView) {
			((ImageView) view).setImageBitmap(bitmap);
		} else if (view instanceof ImageSwitcher) {
			((ImageSwitcher) view).setImageDrawable(new BitmapDrawable(bitmap));
		} else {// 对于其他view设置背景
			view.setBackground(new BitmapDrawable(bitmap));
		}
	}

}
