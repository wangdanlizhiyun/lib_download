package download.otherFileLoader.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.DownloadService;
import download.otherFileLoader.core.DownloadTask;
import download.otherFileLoader.listener.DownloadListener;
import download.otherFileLoader.request.DownFile;
import download.utils.Util;


public class DownFileManager {
	public static SmartExecutor sExecutor;
	public static DLDBManager dldbManager;
	private static ConcurrentHashMap<Integer,DownFile> downingFiles = new ConcurrentHashMap<Integer,DownFile>();
	private static ArrayList<DownloadTask> tasks = new ArrayList<DownloadTask>();

	public static Handler sHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj == null || !(msg.obj instanceof DownFile)){
				return;
			}
			DownFile downFile = null;
			downFile = (DownFile) msg.obj;
			for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
					) {
				DownFile df = entry.getValue();

				if (df.url.equals(downFile.url) && df.downPath.equals(downFile.downPath)) {
					df.state = downFile.state;
					df.downLength = downFile.downLength;
					df.totalLength = downFile.totalLength;
					df.ranges = downFile.ranges;
					df.isCanceled = downFile.isCanceled;
					df.isPaused = downFile.isPaused;
					df.isError = downFile.isError;
					df.isSuppurtRanger = downFile.isSuppurtRanger;
					if (df.listener != null) {
						switch (msg.what){
							case Constants.WHAT_DOWNLOADING:
								df.listener.progress(df.downLength, df.totalLength);
								break;
							case Constants.WHAT_FINISH:
								df.listener.success(df.downPath);
								downingFiles.remove(downFile.hashCode());
								break;
							case Constants.WHAT_ERROR:
								df.listener.error();
								new File(df.downPath,df.name).delete();
								df.downLength = 0;
								df.ranges = null;
								error(df);
								break;
						}
					}
				}
			}


		}
	};

	private static volatile DownFileManager instance = null;
	public static DownFileManager getInstance(Activity activity) {
		if (instance == null) {
			synchronized (DownFileManager.class) {
				if (instance == null) {
					instance = new DownFileManager(activity.getApplication());
				}
			}
		}
		return instance;
	}
	public static DownFileManager getInstance(Service service) {
		if (instance == null) {
			synchronized (DownFileManager.class) {
				if (instance == null) {
					instance = new DownFileManager(service.getApplication());
				}
			}
		}
		return instance;
	}
	public static DownFileManager getInstance() {
		return instance;
	}
	private static Application application;
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private DownFileManager(Application application){
		this.application = application;
		application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

			}

			@Override
			public void onActivityStarted(Activity activity) {

			}

			@Override
			public void onActivityResumed(Activity activity) {

			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		});

		dldbManager = DLDBManager.getInstance(application);
		sExecutor = new SmartExecutor(6, 100);
		sExecutor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		sExecutor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public DownFile initData(String url,String savePath){
		DownFile downFile = new DownFile(url);
		if (TextUtils.isEmpty(savePath)){
			downFile.downPath = Util.getDiskCacheDir(application, "downFile").getAbsolutePath();
		}else {
			downFile.downPath = savePath;
		}
		return dldbManager.queryTaskInfo(downFile);
	}

	public void download(String url) {
		download(url, Util.getDiskCacheDir(application, "downFile").getAbsolutePath(),null);
	}

	public void download(String url,DownloadListener listener) {
		download(url, Util.getDiskCacheDir(application, "downFile").getAbsolutePath(),listener);
	}
	public void download(String url,String savePath) {
		download(url,savePath,null);
	}
	public void download(String url,String savePath,DownloadListener listener) {
		DownFile downFile = new DownFile(url);
		downFile.downPath = savePath;
		downFile.listener = listener;
		Boolean exist = hasIntasks(downFile);
		DownFile downFileT = initData(url, savePath);
		if (downFileT != null){
			downFile.downLength = downFileT.downLength;
			downFile.totalLength = downFileT.totalLength;
			downFile.ranges = downFileT.ranges;
			downFile.isSuppurtRanger = downFileT.isSuppurtRanger;
			downFile.name = downFileT.name;
			if (downFileT.state == DownFile.DownloadStatus.FINISH){
				downFile.state = downFileT.state;
			}
		}
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
			if (df.equals(downFile)) {
				downFile.state = df.state;
				break;
			}
		}
		if (downFile.state == DownFile.DownloadStatus.FINISH){
			if (downFile.listener != null){
				downFile.listener.success(downFile.downPath);
				return;
			}
		}
		downFile.state = DownFile.DownloadStatus.WAITING;
		if (downFile.listener != null){
			downFile.listener.waiting();
		}
		downingFiles.put(downFile.hashCode(), downFile);
		if (!exist){

			Intent it = new Intent(application, DownloadService.class);
			it.putExtra("url",downFile.url);
			it.putExtra("path",downFile.downPath);
			application.startService(it);
		}
	}
	public void addTask(DownFile downFile){
		DownloadTask task = new DownloadTask(downFile);
		tasks.add(task);
		task.start();
	}

	public void pause(String url) {
		pause(url, Util.getDiskCacheDir(application, "downFile").getAbsolutePath());
	}
	public void pause(String url,String savePath) {
		if (TextUtils.isEmpty(savePath)){
			savePath = Util.getDiskCacheDir(application, "downFile").getAbsolutePath();
		}
		DownFile downFile = new DownFile(url,savePath);
		dldbManager.insertOrUpdate(downFile);
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
			if (df.equals(downFile)){
				df.isPaused = true;
				if (df.listener != null){
					df.listener.pause();
				}
			}
		}
		for (DownloadTask task:tasks
			 ) {
			if (task.downFile.equals(downFile)){
				task.pause();
			}
		}
		downFile.isPaused = true;
		if (downFile.listener != null){
			downFile.listener.pause();
		}
	}
	public void cancel(String url) {
		cancel(url,Util.getDiskCacheDir(application, "downFile").getAbsolutePath());
	}
	public static void cancel(String url,String savePath) {
		DownFile downFile = new DownFile(url,savePath);
		cancel(downFile);
	}
	public static void error(DownFile downFile) {
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
			if (df.equals(downFile)){
				df.isError = true;
				if (df.listener != null){
					df.listener.error();
				}
				new File(df.downPath,df.name).delete();
				downFile.downLength = 0;
				downFile.ranges = null;
			}
		}
		for (DownloadTask task:tasks
				) {
			if (task.downFile.equals(downFile)){
				task.error();
				break;
			}
		}
	}
	public static void cancel(DownFile downFile) {
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
			if (df.equals(downFile)){
				df.isCanceled = true;
				if (df.listener != null){
					df.listener.cancel();
				}
				new File(df.downPath,df.name).delete();
				downFile.downLength = 0;
				downFile.ranges = null;
				dldbManager.deleteTaskInfo(downFile);
			}
		}
		for (DownloadTask task:tasks
				) {
			if (task.downFile.equals(downFile)){
				task.cancel();
				tasks.remove(task);
				break;
			}
		}
	}


	/**
	 * 暂停所有下载
	 */
	public void pauseAll() {
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
				df.isPaused = true;
				if (df.listener != null){
					df.listener.pause();
					dldbManager.insertOrUpdate(df);
				}
		}
		for (DownloadTask task:tasks
				) {
				task.pause();
		}

	}

	/**
	 * 恢复所有网络失败的下载
	 */
	public void recoverAllNetError() {
		for (DownloadTask task:tasks
				) {
			if (task.isErrored == true){
				task.start();
			}
		}
	}

	/**
	 * 全部恢复下载
	 */
	public void recoverAll() {
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
			df.isPaused = false;
		}
		for (DownloadTask task:tasks
				) {
			task.start();
		}
	}
	private Boolean hasIntasks(DownFile downFile){
		for (DownloadTask task:tasks
				) {
			if (task != null && !task.isCancelled && !task.isPaused && task.downFile.equals(downFile)){
				return true;
			}
		}
		return false;
	}
}
