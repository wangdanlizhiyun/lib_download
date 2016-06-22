package download.otherFileLoader.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.DownloadTask;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.PackageUtil;
import download.otherFileLoader.util.ToastUtils;
import download.utils.Util;


public class DownFileManager {
	private SmartExecutor executor;
	private static DLDBManager dldbManager;
	private static ArrayList<DownFile> downingFiles = new ArrayList<DownFile>();
	private ArrayList<DownloadTask> tasks = new ArrayList<DownloadTask>();

	private static Handler sHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			DownFile downFile = (DownFile) msg.obj;
			if (downFile == null) {
				return;
			}
			for (DownFile df:downingFiles
					) {
				if (df.url.equals(downFile.url) && df.downPath.equals(downFile.downPath)) {
					df.state = downFile.state;
					df.downLength = downFile.downLength;
					df.totalLength = downFile.totalLength;
					df.ranges = downFile.ranges;
					df.isCanceled = downFile.isCanceled;
					df.isPaused = downFile.isPaused;
					df.isSuppurtRanger = downFile.isSuppurtRanger;
					if (df.listener != null) {
						switch (msg.what){
							case Constants.WHAT_DOWNLOADING:
											df.listener.progress(df.downLength, df.totalLength);
								break;
							case Constants.WHAT_FINISH:
								if (df.isAutoInstall) {
									Boolean code = PackageUtil.install(context, new File(downFile.downPath));
									if (!code) {
										ToastUtils.showToast(context, "安装失败");
									}
								}
											df.listener.success(df.downPath);
								break;
							case Constants.WHAT_ERROR:
											Bundle b = msg.getData();
											df.listener.error(b.getString("error"));

								df.getDownloadFile().delete();
								df.downLength = 0;
								df.ranges = null;
								dldbManager.insertOrUpdate(downFile);
								break;
						}
					}
				}
			}
			if (msg.what == Constants.WHAT_ERROR){
				dldbManager.deleteTaskInfo(downFile);
			}else {
				dldbManager.insertOrUpdate(downFile);
			}

		}
	};

	private static volatile DownFileManager instance = null;
	public static DownFileManager getInstance(Context context) {
		if (instance == null) {
			synchronized (DownFileManager.class) {
				if (instance == null) {
					instance = new DownFileManager(context);
				}
			}
		}
		return instance;
	}
	private static Context context;
	private DownFileManager(Context context){
		this.context = context;
		dldbManager = DLDBManager.getInstance(context);
		executor = new SmartExecutor(6, 100);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public DownFile initData(DownFile downFile){
		if (TextUtils.isEmpty(downFile.downPath)){
			downFile.downPath = Util.getDiskCacheDir(context,"downFile").getAbsolutePath();
		}
		DownFile downFileT = dldbManager.queryTaskInfo(downFile);
		if (downFileT != null){
			downFile.downLength = downFileT.downLength;
			downFile.totalLength = downFileT.totalLength;
			downFile.ranges = downFileT.ranges;
			downFile.isSuppurtRanger = downFileT.isSuppurtRanger;
			downFile.name = downFileT.name;
			if (downFileT.state == Constants.DOWNLOAD_STATE_FINISH){
				downFile.state = Constants.DOWNLOAD_STATE_FINISH;
			}
		}
		for (DownloadTask task:tasks
				) {
			if (task != null && !task.isCancelled && !task.isPaused && task.downFile.equals(downFile)){
				downFile.state = task.downFile.state;
				break;
			}
		}
		return downFile;
	}


	public void down(DownFile downFile) {
		Boolean exist = hasIntasks(downFile);
		downFile = DownFileManager.getInstance(context).initData(downFile);
		if (downFile.state == Constants.DOWNLOAD_STATE_FINISH){
			if (downFile.listener != null){
				downFile.listener.success(downFile.downPath);
				return;
			}
		}
		downingFiles.add(downFile);
		if (!exist){
			DownloadTask task = new DownloadTask(downFile,sHandler,executor);
			tasks.add(task);
			task.start();
		}
	}
	public void pause(DownFile downFile) {
		for (DownFile df:downingFiles
				) {
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
	}
	public void cancel(DownFile downFile) {
		for (DownFile df:downingFiles
				) {
			if (df.equals(downFile)){
				df.isCanceled = true;
				if (df.listener != null){
					df.listener.cancel();
				}
				downFile.getDownloadFile().delete();
				downFile.downLength = 0;
				downFile.ranges = null;
				dldbManager.insertOrUpdate(downFile);
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


	public void pauseAll() {
		for (DownFile df:downingFiles
				) {
				df.isPaused = true;
				if (df.listener != null){
					df.listener.pause();
				}
		}
		for (DownloadTask task:tasks
				) {
				task.pause();
		}

	}

	public void recoverAll() {
		for (DownFile df:downingFiles
				) {
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
