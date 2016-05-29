package download.imageLoader.core;

import android.content.Context;
import android.view.View;
import download.imageLoader.listener.BackListener;
import download.imageLoader.listener.BackListenerAdapter;
import download.imageLoader.listener.CustomDisplayMethod;
import download.imageLoader.request.BitmapRequest;

/**
 * 为了调用更简单添加此门面
 * path示例："http://img.blog.csdn.net/20160114230048304",//gif图
 * "assets//:test.png",
 * "drawable//:"+R.drawable.common_logo,
 * "file:///mnt/sdcard/paint.png",
 * @author lizhiyun
 *
 */
public class BmLoader {

	/**
	 * preload
	 * 
	 * @param path
	 */
	public static void preLoad(String path) {
		ImageLoader.getInstance().preLoad(path);
	}
	/**
	 * 
	 * @param path
	 * @param view
	 * @param listener
	 */
	public static void load(String path, View view, BackListener listener) {
		ImageLoader.getInstance().loadImage(path, view, listener);
	}

	/**
	 *
	 * @param path
	 * @param view
	 * @param listener
	 */
	public static void load(String path, View view, BackListenerAdapter listener) {
		ImageLoader.getInstance().loadImage(path, view, listener);
	}

	/**
	 * 
	 * @param path
	 * @param view
	 */
	public static void load(String path, View view) {
		ImageLoader.getInstance().loadImage(path, view);
	}

	/**
	 *
	 * @param path
	 * @param view
	 * @param customDisplayMethod
	 */
	public static void load(String path, View view,CustomDisplayMethod customDisplayMethod) {
		ImageLoader.getInstance().loadImage(path, view,customDisplayMethod);
	}

	/**
	 * set longding and load failed bitmap resource.
	 * @param context
	 * @param loadingId
	 * @param failedId
	 */
	public static void setLoadingAndFailedId(Context context, int loadingId,
											 int failedId) {
		ImageLoader.getInstance().setLoadingAndFailedId(context, loadingId, failedId);
	}

	/**
	 *
	 * @param path
	 * @param view
	 * @param customDisplayMethod 自定义显示方式
	 */
	public static void loadImage(String path,View view,CustomDisplayMethod customDisplayMethod) {
		ImageLoader.getInstance().loadImage(path, view, customDisplayMethod);
	}
	/**
	 *
	 * @param path
	 * @param view
	 * @param customDisplayMethod 自定义显示方式
	 */
	public static void loadImage(String path, View view,int width,int height,
							 CustomDisplayMethod customDisplayMethod) {
		ImageLoader.getInstance().loadImage(path, view, customDisplayMethod);
	}

	/**
	 * 设置是否仅wifi下下载模式
	 * @param b
	 */
	public static void setOnlyWifiMode(Boolean b){
		ImageLoader.getInstance().getConfig().setOnlyWifiMode(b);
	}

	/**
	 * 设置是否仅使用缓存模式
	 * @param b
	 */
	public static void setOnlyMemoryMode(Boolean b){
		ImageLoader.getInstance().getConfig().setOnlyMemoryMode(b);
	}

	/**
	 * clear all memory
	 */
	public static void clearAllMemory(){
		ImageLoader.getInstance().getConfig().cache.clearMemory();
		ImageLoader.getInstance().getConfig().cache.clearDiskMemory();
	}

}
