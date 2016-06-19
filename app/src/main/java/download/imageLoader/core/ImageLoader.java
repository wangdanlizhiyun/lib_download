package download.imageLoader.core;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.view.PowerImageView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

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
		executor = new SmartExecutor(threadCount, 400);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
		config = new ImageConfig();
	}

	protected void setLoadingAndFailedId(Context context, int loadingId,
			int failedId) {
		config.setFailedIdAndLoadingId(context.getResources(), failedId,
				loadingId);
	}


	protected void preLoad(String path) {
		BitmapRequest request = new BitmapRequest();
		request.path = path;
		loadImage(request);
	}

	public void cancelOldTask(PowerImageView powerImageView) {
		executor.remove(powerImageView);
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void loadImage(final BitmapRequest request) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			if (request.view == null || request.view.get() == null){
				final LoadTask task = new LoadTask(request, ImageLoader.this);
				executor.execute(task);
			}else {
				getInstance();
				config.cache.setDiskLruCache(request.view.get().getContext()
						.getApplicationContext());
				config.initDefault(request.view.get().getContext());
				config.cache.getMemoryCache(request);
				if (!request.checkIfNeedAsyncLoad()) {
					request.display();
				} else {
					executor.remove(request.view.get());
					final LoadTask task = new LoadTask(request, ImageLoader.this);
					request.displayLoading(config.getLoadingBm());
					request.view.get().setTag(request.getKey());
							if (request.checkEffective()) {
								executor.execute(task);
							}
				}
			}
		} else {
			throw new RuntimeException("only run on ui thread");
		}
	}

}