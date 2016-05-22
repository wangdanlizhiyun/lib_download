package download.imageLoader.core;

/**
 * 为避免错位请尽量在ui线程调用,对于1m以上的大图实现了渐进式的图片加载效果，小图第一次下载为淡出效果
 */

import java.util.LinkedHashMap;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.listener.BackListener;
import download.imageLoader.loader.Load;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.DownloadBitmapUtils;
import download.imageLoader.util.UrlParser;
import download.imageLoader.view.GifMovieView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ImageLoader {
	private static SmartExecutor executor;

	private static class InstanceHoler {
		private static final ImageLoader instance = new ImageLoader();
	}

	public static ImageLoader getInstance() {
		return InstanceHoler.instance;
	}

	private static ImageConfig config = null;
	private final static int REFRESH = 1;
	private final static int threadCount = 5;
	private static Handler mUIHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			final BitmapRequest request = (BitmapRequest) msg.obj;
			if (request == null) {
				return;
			}
			switch (msg.what) {
			case REFRESH:
				try {
					if (((String) request.view.getTag()).equals(request.path)) {
						if (request.listener == null) {
							if (request.checkEmpty()) {
								setBitmap(request.view, config.getFailedBm());
							} else {
								if (request.isAnimation && request.bitmap != null) {
									request.view.post(new Runnable() {

										@Override
										public void run() {
											// 作为独立jar包不便用属性动画
											final TransitionDrawable td = new TransitionDrawable(
													new Drawable[] {
															new ColorDrawable(
																	Color.TRANSPARENT),
															new BitmapDrawable(
																	request.view
																			.getResources(),
																	request.bitmap) });
											if (request.view instanceof ImageView) {
												((ImageView) request.view)
														.setImageDrawable(td);
											} else {
												request.view.setBackground(td);
											}
											td.startTransition(500);
										}
									});
								} else {
									setBitmap(request);
								}
							}
						} else {
							// 设置了回调监听的自己处理
							request.listener.onProcess(request.percent);
							request.listener.onSuccess(request.bitmap,request.movie);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}

		}

	};

	private static void setBitmap(View view,Bitmap bitmap) {
		if (view == null) {
			return;
		}
		if (bitmap == null) {
			setBitmap(view, config.getFailedBm());
		}

		if (view instanceof ImageView) {
			((ImageView) view).setImageBitmap(bitmap);
		} else if (view instanceof ImageSwitcher) {
			((ImageSwitcher) view).setImageDrawable(new BitmapDrawable(view
					.getResources(), bitmap));
		} else {// 对于其他自定义view
			view.setBackground(bitmap == null ? null : new BitmapDrawable(view
					.getResources(), bitmap));
		}
	}
	private static void setBitmap(BitmapRequest request) {
		if (request.view instanceof GifMovieView){
			if (request.movie != null){
				((GifMovieView) request.view).setMovie(request.movie);
			}else {
				if (request.bitmap != null){
					((GifMovieView) request.view).setImageBitmap(request.bitmap);
				}else {
					((GifMovieView) request.view).setImageBitmap(config.getFailedBm());
				}
			}
		}else{
			setBitmap(request.view,request.bitmap);
		}
	}

	private ImageLoader() {
		executor = new SmartExecutor(threadCount, 200);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
		config = new ImageConfig();
	}

	public ImageLoader setLoadingAndFailedId(Context context, int loadingId,
			int failedId) {
		config.setFailedIdAndLoadingId(context.getResources(), failedId,
				loadingId);
		return getInstance();
	}


	protected void loadImage(final String path, final View view,
			BackListener listener) {
		getInstance();
		BitmapRequest request = new BitmapRequest(view, path);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			config.cache.setDiskLruCache(request.view.getContext()
					.getApplicationContext());
			config.cache.getMemoryCache(request);
			if (!request.checkEmpty()) {
				Log.v("test","use cache");
				setBitmap(request);
			} else {
				Log.v("test","not use cache");
				setBitmap(request.view,config.getLoadingBm());
				request.view.setTag(request.path);
				executor.execute(buildTask(new BitmapRequest(request.view,
						request.path, listener)));
			}
		} else {
			throw new RuntimeException("only run on ui thread");
		}
	}

	/**
	 * 预加载图片
	 * 
	 * @param path
	 */
	protected void preLoad(final String path) {
		BitmapRequest request = new BitmapRequest(null, path);
		config.cache.getDiskCacheBitmap(request);
		if (request.checkEmpty()) {
			executor.execute(buildTask(new BitmapRequest(null, path)));
		}
	}

	protected void loadImage(final String path, final View view) {
		loadImage(path, view, null);
	}

	private LinkedHashMap<String, String> doingMap = new LinkedHashMap<String, String>(
			threadCount);

	private Runnable buildTask(final BitmapRequest request) {
		return new Runnable() {
			@Override
			public void run() {
				//listview等快速滑动时过滤应该放弃的任务
				if (request.view != null && !((String) request.view.getTag()).equals(request.path)) {
					return;
				}
				while (hasDoingTask(request)) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				addDoingTask(request);
				config.cache.getMemoryCache(request);
				if (request.checkEmpty()){
					config.cache.getDiskCacheBitmap(request);
				}
				if (request.checkEmpty()) {
					Load.loadBitmap(request, config, new BackListener() {

						@Override
						public void onProcess(int percent) {
							if (request.view == null) {
								return;
							}
							// 没设置回调的话，默认大图边加载边显示，小图第一次通过加载网谈出显示
							if (request.listener == null) {
								if (request.totalSize > DownloadBitmapUtils.bigSize) {
									if (percent % 10 != 0) {
										return;
									}
									config.cache
											.getDiskCacheBitmap(request);
									config.cache.addPercentBitmap(request.path,
											request.bitmap);
									refreashBitmap(request, false);
								}
							} else {
								refreashBitmap(request, false);
							}
						}

						@Override
						public void onSuccess(Bitmap bitmap, Movie movie) {
						}
					});
					// 第一次下载时。大文件分段显示，小文件动画
					if (request.view != null) {
						refreashBitmap(request,
								request.totalSize < DownloadBitmapUtils.bigSize
										&& UrlParser.isNetWork(request.path));
					}
				} else {
					refreashBitmap(request,false);
				}
				config.cache.addMemoryBitmap(request);
				removeDoingTask(request);
			}
		};
	}

	private synchronized Boolean hasDoingTask(BitmapRequest request) {
		synchronized (doingMap) {
			return doingMap.containsKey(request.path);
		}
	}

	private synchronized void addDoingTask(BitmapRequest request) {
		synchronized (doingMap) {
			doingMap.put(request.path, request.path);
		}
	}

	private synchronized void removeDoingTask(BitmapRequest request) {
		synchronized (doingMap) {
			doingMap.remove(request.path);
		}
	}

	private void refreashBitmap(BitmapRequest request,
			Boolean isAnimation) {
		Message message = Message.obtain();
		message.what = REFRESH;
		request.isAnimation = isAnimation;
		message.obj = request;
		mUIHandler.sendMessage(message);
	}
}