package download.otherFileLoader.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.request.DownFile;
import download.otherFileLoader.util.PackageUtil;
import download.otherFileLoader.util.ToastUtils;
import download.utils.Util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ApkLoader {
	private SmartExecutor executor;
	private static Context mContext;
	private static Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			DownFile downFile = (DownFile) msg.obj;
			if (downFile == null) {
				ToastUtils.showToast(mContext, "下载失败");
				return;
			}
			if (downFile.getDownLength() >= downFile.getTotalLength()) {
				
				Log.v("test", "下载了"+downFile.getName()+"包名"+downFile.getPakageName());
				Boolean code = PackageUtil.install(mContext,new File(downFile.getDownPath()));
				if (!code) {
					ToastUtils.showToast(mContext, "安装失败");
				}
			} else if (downFile.getDownLength() < 0) {
				ToastUtils.showToast(mContext, downFile.getName() + "下载失败");
			} else {
				
				
			}
		}
	};
	private static volatile ApkLoader instance = null;

	public static ApkLoader getInstance(Context context) {
		if (instance == null) {
			synchronized (ApkLoader.class) {
				if (instance == null) {
					instance = new ApkLoader(context.getApplicationContext());
				}
			}
		}
		return instance;
	}

	public DownloadFileRunnable buildTask(final DownFile downFile,
			DownloadFileRunnable.DownloadPercentListener listener) {
		try {
			File saveFile = new File(Util.getDiskCacheDir(mContext, "apkFile")
					.getAbsolutePath(), Util.getNameFromUrl(downFile.downUrl));
			Log.v("test", "保存目录"+saveFile.getParent());
			if (!saveFile.getParentFile().exists()) {
				Boolean b = saveFile.getParentFile().mkdirs();
				if (!b) {
					Log.v("test", "新建apkFile文件失败");
				}
			}
			if (!saveFile.exists()) {
				saveFile.createNewFile();
				// 删除原来的下载数据
				DownFileManager.getInstance(mContext).delete(downFile.getDownUrl());
			}
			downFile.setDownPath(saveFile.getAbsolutePath());
			return new DownloadFileRunnable(downFile, saveFile, this, listener);
		} catch (IOException e) {
			Toast.makeText(mContext, "无法写入", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return null;
	}

	public void downFile(String path, DownloadFileRunnable.DownloadPercentListener listener) {
		DownFile downFile = new DownFile(path);
		// 本地数据
		DownFile mDownFileT = DownFileManager.getInstance(mContext).getDownFile(downFile.getDownUrl());
		
		if (mDownFileT != null) {
			downFile = mDownFileT;
			if (downFile.getDownLength() != downFile.getTotalLength()) {
				downFile.state = 0;
			}
			if (downFile.getState() == 1) {// 已下载完成就返回
				listener.notify(mDownFileT);
				return;
			}else {
				ToastUtils.showToast(mContext, "开始下载");
			}
		}
		DownFileManager.getInstance(mContext).save(downFile);
		if (downFile.state == 2) {// 正在下载
			return;
		}
		DownloadFileRunnable task = buildTask(downFile, listener);
		if (task != null) {
			downFile.state = 2;
			executor.execute(task);
		}
	}

	private void downApk(String path, DownloadFileRunnable.DownloadPercentListener downloadPercentListener) {
		downFile(path, new DownloadFileRunnable.DownloadPercentListener() {

			@Override
			public void notify(DownFile downFile) {
				if (downFile.state == 1) {
					downFile.setPakageName(PackageUtil.getApkName(mContext, downFile.getDownPath()));
					ApkLoader.this.update(downFile);
				}
				if (downFile.state == 1 || System.currentTimeMillis() - downFile.lastNotifyTime > 2000) {
					Message msg = Message.obtain();
					msg.obj = downFile;
					mUIHandler.sendMessage(msg);
					downFile.lastNotifyTime = System.currentTimeMillis();
				}
			}
		});
	}

	private ApkLoader(Context context) {
		ApkLoader.mContext = context;
		executor = new SmartExecutor(3, 40);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	public void update(DownFile downFile) {
		DownFileManager.getInstance(mContext).save(downFile);
	}
	/**
	 * 取消下载的任务
	 * @param path
	 */
	public void cancel(String path){
		List<Runnable> runningList = executor.getWaitingList();
		for (Runnable runnable : runningList) {
			if (runnable instanceof DownloadFileRunnable) {
				DownloadFileRunnable dr = (DownloadFileRunnable) runnable;
				if (dr.getmDownFile().downUrl.equals(path)) {
					executor.cancelWaitingTask(runnable);
				}
			}
		}
		List<Runnable> runningLis = executor.getRunningList();
		for (Runnable runnable : runningLis) {
			if (runnable instanceof DownloadFileRunnable) {
				DownloadFileRunnable dr = (DownloadFileRunnable) runnable;
				if (dr.getmDownFile().downUrl.equals(path)) {
					dr.cancel();
				}
			}
		}
	}

}
