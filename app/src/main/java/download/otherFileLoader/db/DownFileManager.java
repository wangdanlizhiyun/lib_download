package download.otherFileLoader.db;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.core.DownloadService;
import download.otherFileLoader.listener.DownloadListener;
import download.otherFileLoader.request.DownFile;
import download.utils.Util;


public class DownFileManager {
	private LiteOrm orm;

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
	public synchronized void  save(DownFile mDownFile){
		orm.save(mDownFile);
	}


	public void down(DownFile build) {
		Log.e("test","build.downPath="+build.downPath);
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, build);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
		context.startService(intent);
	}
	public void pause(DownFile build) {
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, build);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
		context.startService(intent);
	}
	public void cancel(DownFile build) {
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, build);
		intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
		context.startService(intent);
	}

	public void pauseAll() {


	}

	public void recoverAll() {

	}
}
