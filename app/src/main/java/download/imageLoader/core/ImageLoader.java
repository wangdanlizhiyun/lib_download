package download.imageLoader.core;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ViewTaskUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {
	private SmartExecutor executor;
	private static class InstanceHoler {
		private static final ImageLoader instance = new ImageLoader();
	}
	public static ImageLoader getInstance() {
		return InstanceHoler.instance;
	}

	private RunningTasksManager mRunningTasksManager;
	private ImageConfig config = null;
	private final int threadCount = 3;

	public RunningTasksManager getmRunningTasksManager(){
		if (mRunningTasksManager == null)
			mRunningTasksManager = new RunningTasksManager(threadCount);
		return mRunningTasksManager;
	}
	public ImageConfig getConfig(){
		return config;
	}
	private ImageLoader() {
		mRunningTasksManager = new RunningTasksManager(threadCount);
		executor = new SmartExecutor(threadCount, 200);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
		config = new ImageConfig();
	}

	protected void setLoadingAndFailedId(Context context, int loadingId,
			int failedId) {
		config.setFailedIdAndLoadingId(context.getResources(), failedId,
				loadingId);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(final String path, final View view,
			final BackListener listener) {
		getInstance();
		if (Looper.myLooper() == Looper.getMainLooper()) {
			view.post(new Runnable() {
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
				public void run() {
					BitmapRequest request = new BitmapRequest(view, path,listener);
					config.cache.setDiskLruCache(request.view.get().getContext()
							.getApplicationContext());
					config.cache.getMemoryCache(request);
					if (!request.checkIfNeedAsyncLoad()) {
						request.display();
					} else {
						ViewTaskUtil.cancelOldTask(executor,view);
						LoadTask task = new LoadTask(request,ImageLoader.this);
						if (view instanceof ImageView){
							((ImageView) view).setImageBitmap(config.getLoadingBm());
						}else {
							view.setBackground(new BitmapDrawable(view.getResources(),config.getLoadingBm()));
						}

						view.setTag(request.path);
						executor.execute(task);
					}
				}
			});
		} else {
			throw new RuntimeException("only run on ui thread");
		}
	}

	protected void preLoad(final String path) {
		BitmapRequest request = new BitmapRequest(null, path);
		config.cache.getDiskCacheBitmap(request);
		if (request.checkIfNeedAsyncLoad()) {
			executor.execute(new LoadTask(request,ImageLoader.this));
		}
	}

	protected void loadImage(final String path, final View view) {
		loadImage(path, view, null);
	}
}