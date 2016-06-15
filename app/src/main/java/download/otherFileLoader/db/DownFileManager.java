package download.otherFileLoader.db;

import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import com.litesuits.orm.LiteOrm;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.DownloadTask;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.PackageUtil;
import download.otherFileLoader.util.ToastUtils;
import download.utils.Util;


public class DownFileManager {
	private LiteOrm orm;private SmartExecutor executor;
	private ArrayList<DownFile> downingFiles = new ArrayList<DownFile>();

	private Handler sHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			DownFile downFile = (DownFile) msg.obj;
			if (downFile == null) {
				return;
			}
			switch (msg.what){
				case Constants.WHAT_DOWNLOADING:
					for (DownFile df:downingFiles
							) {
						if (df.equals(downFile)) {
							if (df.listener != null) {
								df = downFile;
								df.listener.progress(df.downLength,df.totalLength);
							}
						}
					}
					break;
				case Constants.WHAT_FINISH:
					if (downFile.isAutoInstall) {
						Boolean code = PackageUtil.install(context, new File(downFile.downPath));
						if (!code) {
							ToastUtils.showToast(context, "安装失败");
						}
					}
					for (DownFile df:downingFiles
							) {
						if (df.equals(downFile)){
							if (df.listener != null){
								df = downFile;
								df.listener.success(df.downPath);
							}
						}
					}
					break;
				case Constants.WHAT_PROCESS:
					for (DownFile df:downingFiles
							) {
						if (df.equals(downFile)){
							if (df.listener != null){
								df.listener.progress(downFile.downLength,downFile.totalLength);
							}
						}
					}
					break;
				case Constants.WHAT_ERROR:
					for (DownFile df:downingFiles
							) {
						if (df.equals(downFile)){
							if (df.listener != null){
								Bundle b = msg.getData();
								df.listener.error(b.getString("error"));
							}
						}
					}
					break;
			}
			save(downFile);

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
	private Context context;
	private DownFileManager(Context context){
		this.context = context;
		orm = LiteOrm.newSingleInstance(context, "downfile");
		executor = new SmartExecutor(4, 100);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public void initData(DownFile downFile){
		DownFile downFileT = getDownFile(downFile);
		if (downFileT != null){
			downFile = downFileT;
			downFile.downLength = downFileT.downLength;
			downFile.totalLength = downFileT.totalLength;
			downFile.ranges = downFileT.ranges;
		}
		if (TextUtils.isEmpty(downFile.downPath)){
			downFile.downPath = Util.getDiskCacheDir(context,"downFile").getAbsolutePath();
		}
	}
	public DownFile getDownFile(DownFile downFile){
		return orm.queryById(downFile.url, DownFile.class);
	}
	public void  save(DownFile mDownFile){
		orm.save(mDownFile);
	}


	public void down(DownFile downFile) {
		Log.e("test","downFile.downPath="+downFile.downPath);

		Boolean exist = hasIntasks(downFile);
		DownFileManager.getInstance(context).initData(downFile);
		downingFiles.add(downFile);
		if (!exist){
			new DownloadTask(downFile,sHandler,executor).start();
		}
	}
	public void pause(DownFile downFile) {
		for (DownFile df:downingFiles
				) {
			if (df.equals(downFile)){
				df.isPaused = true;
			}
		}
	}
	public void cancel(DownFile downFile) {
		for (DownFile df:downingFiles
				) {
			if (df.equals(downFile)){
				df.isCanceled = true;
			}
		}
	}


	public void pauseAll() {


	}

	public void recoverAll() {

	}
	private Boolean hasIntasks(DownFile downFile){
		for (DownFile df:downingFiles
				) {
			if (df.equals(downFile)){
				return true;
			}
		}
		return false;
	}
}
