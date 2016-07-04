package download.otherFileLoader.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.DownloadTask;
import download.otherFileLoader.request.DownBuilder;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.PackageUtil;
import download.otherFileLoader.util.ToastUtils;


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
								downingFiles.remove(downFile.hashCode());
								break;
							case Constants.WHAT_ERROR:
								df.listener.error();
								new File(df.downPath,df.name).delete();
								df.downLength = 0;
								df.ranges = null;
								cancel(df);
								break;
						}
					}
				}
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
		this.context = context.getApplicationContext();
		dldbManager = DLDBManager.getInstance(this.context);
		sExecutor = new SmartExecutor(6, 100);
		sExecutor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		sExecutor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public DownFile initData(DownFile downFile){
		return dldbManager.queryTaskInfo(downFile);
	}


	public void down(DownFile downFile) {
		Boolean exist = hasIntasks(downFile);
		DownFile downFileT = DownFileManager.getInstance(context).initData(downFile);
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
			DownloadTask task = new DownloadTask(downFile);
			tasks.add(task);
			task.start();
		}
	}
	public void pause(DownFile downFile) {
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


	public void pauseAll() {
		for (Map.Entry<Integer,DownFile> entry:downingFiles.entrySet()
				) {
			DownFile df = entry.getValue();
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
