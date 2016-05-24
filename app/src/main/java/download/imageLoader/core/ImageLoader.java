package download.imageLoader.core;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.entity.CustomDrawable;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ViewTaskUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
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
	private static ImageConfig config = null;
	private final static int threadCount = 5;

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
					} else if (ViewTaskUtil.cancelOldTask(executor,path,view)){
						LoadTask task = new LoadTask(request,ImageLoader.this);
						CustomDrawable loadingDrawable = new CustomDrawable(view.getResources(),config.getLoadingBm(),task);
						if (view instanceof ImageView){
							((ImageView) view).setImageDrawable(loadingDrawable);
						}else {
							view.setBackground(loadingDrawable);
							view.setTag(task);
						}
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