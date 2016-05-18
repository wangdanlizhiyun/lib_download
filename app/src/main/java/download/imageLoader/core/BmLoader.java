package download.imageLoader.core;

import android.view.View;
import download.imageLoader.listener.BackListener;
/**
 * 为了调用更简单添加此门面
 * @author lizhiyun
 *
 */
public class BmLoader {
	/**
	 * 预加载图片
	 * 
	 * @param path
	 */
	public static void preLoad(String path) {
		ImageLoader.getInstance().preLoad(path);
	}
	/**
	 * 
	 * @param path示例："http://img.blog.csdn.net/20160114230048304",//gif图
 	 * "assets//:test.png",
     * "drawable//:"+R.drawable.common_logo,
     * "file:///mnt/sdcard/paint.png",
	 * @param view
	 * @param listener
	 */
	public static void load(final String path, final View view, BackListener listener) {
		ImageLoader.getInstance().loadImage(path, view, listener);
	}

	/**
	 * 
	 * @param path示例："http://img.blog.csdn.net/20160114230048304",//gif图
 	 * "assets//:test.png",
     * "drawable//:"+R.drawable.common_logo,
     * "file:///mnt/sdcard/paint.png",
	 * @param view
	 */
	public static void load(final String path, final View view) {
		ImageLoader.getInstance().loadImage(path, view);
	}
}
