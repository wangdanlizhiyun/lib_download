package download.otherFileLoader.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private SmartExecutor executor;
	private static DLDBManager dldbManager;
	private static HashMap<Integer,DownFile> downingFiles = new HashMap<Integer,DownFile>();
	private static ArrayList<DownloadTask> tasks = new ArrayList<DownloadTask>();

	private static Handler sHandler = new Handler(Looper.getMainLooper()){
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
								dldbManager.insertOrUpdate(downFile);
								break;
							case Constants.WHAT_FINISH:
								if (df.isAutoInstall) {
									Boolean code = PackageUtil.install(context, new File(downFile.downPath));
									if (!code) {
										ToastUtils.showToast(context, "安装失败");
									}
								}
											df.listener.success(df.downPath);
								dldbManager.insertOrUpdate(downFile);
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
		executor = new SmartExecutor(6, 100);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public DownFile initData(DownFile downFile){
		DownFile downFileT = dldbManager.queryTaskInfo(downFile);
		if (downFileT != null){
			downFile.downLength = downFileT.downLength;
			downFile.totalLength = downFileT.totalLength;
			downFile.ranges = downFileT.ranges;
			downFile.isSuppurtRanger = downFileT.isSuppurtRanger;
			downFile.name = downFileT.name;
			if (downFileT.state == DownFile.DownloadStatus.FINISH){
				downFile.state = DownFile.DownloadStatus.FINISH;
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
			DownloadTask task = new DownloadTask(dldbManager,downFile,sHandler,executor);
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
				downingFiles.remove(df.hashCode());
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
	public static  void removeDownFiles(ArrayList<DownFile> mDownloadEntries){

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
