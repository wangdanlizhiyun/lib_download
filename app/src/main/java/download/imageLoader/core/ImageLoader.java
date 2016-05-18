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
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
							if (request.bitmap == null) {
								setBitmap(request.view, config.getFailedBm());
							} else {
								if (request.isAnimation) {
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
									setBitmap(request.view, request.bitmap);
								}
							}
						} else {
							// 设置了回调监听的自己处理
							request.listener.onProcess(request.percent);
							request.listener.onSuccess(request.bitmap);
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

	private static void setBitmap(View view, Bitmap bm) {
		if (view instanceof ImageView) {
			((ImageView) view).setImageBitmap(bm);
		} else if (view instanceof ImageSwitcher) {
			((ImageSwitcher) view).setImageDrawable(new BitmapDrawable(view
					.getResources(), bm));
		} else {// 对于其他自定义view
			view.setBackground(bm == null ? null : new BitmapDrawable(view
					.getResources(), bm));
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

	/**
	 * 
	 * @param path示例
	 *            ："http://img.blog.csdn.net/20160114230048304",//gif图
	 *            "assets//:test.png", "drawable//:"+R.drawable.common_logo,
	 *            "file:///mnt/sdcard/paint.png",
	 * @param view
	 * @param mm
	 */
	protected void loadImage(final String path, final View view,
			BackListener listener) {
		getInstance();
		BitmapRequest request = new BitmapRequest(view, path);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			config.cache.setDiskLruCache(request.view.getContext()
					.getApplicationContext());
			Bitmap bm = config.cache.getMemoryCacheBitmap(request);
			if (bm != null) {
				setBitmap(request.view, bm);
			} else {
				setBitmap(request.view, config.getLoadingBm());
				request.view.setTag(request.path);
				executor.execute(buildTask(new BitmapRequest(request.view,
						request.path, listener)));
			}
		} else {
			throw new RuntimeException("请在主线程调用");
		}
	}

	/**
	 * 预加载图片
	 * 
	 * @param path
	 */
	protected void preLoad(final String path) {
		if (config.cache.getDiskCacheBitmap(new BitmapRequest(null, path)) == null) {
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
				while (hasDoingTask(request)) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				addDoingTask(request);
				Bitmap bm = config.cache.getBitmap(request);
				if (bm == null) {
					bm = Load.loadBitmap(request, config, new BackListener() {

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
									Bitmap percentBitmap = config.cache
											.getDiskCacheBitmap(request);
									config.cache.addPercentBitmap(request.path,
											percentBitmap);
									refreashBitmap(request, percentBitmap,
											false);
								}
							} else {
								refreashBitmap(request, null, false);
							}
						}

						@Override
						public void onSuccess(Bitmap bitmap) {
						}
					});
					// 第一次下载时。大文件分段显示，小文件动画
					if (request.view != null) {
						refreashBitmap(request, bm,
								request.totalSize < DownloadBitmapUtils.bigSize
										&& UrlParser.isNetWork(request.path));
					}
				} else {
					refreashBitmap(request, bm, false);
				}
				config.cache.addMemoryBitmap(request.path, bm);
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

	private void refreashBitmap(final BitmapRequest request, Bitmap bm,
			Boolean isAnimation) {
		Message message = Message.obtain();
		message.what = REFRESH;
		request.bitmap = bm;
		request.isAnimation = isAnimation;
		message.obj = request;
		mUIHandler.sendMessage(message);
	}
}