package download.otherFileLoader.request;

import android.text.TextUtils;
import java.io.File;
import java.util.HashMap;

import download.otherFileLoader.core.Constants;
import download.otherFileLoader.listener.DownloadListener;
import download.utils.Util;

public class DownFile{

	private DownFile() {
	}

	public DownFile(String downUrl) {
		super();
		this.url = downUrl;
		this.name = Util.getNameFromUrl(url);
		state = 0;
		this.isCanceled = false;
		this.isPaused = false;
		this.isSuppurtRanger = false;
		this.isInstall = false;
	}
	public DownloadListener listener;
	public volatile boolean isCanceled = false;
	public volatile boolean isPaused = false;
	public long lastNotifyTime;
	public String icon = "";
	public String name = "";
	public String description = "";
	public Boolean isSuppurtRanger = false;
	public String pakageName = "";
	/** public static final int DOWNLOAD_STATE_IDLE = 0;
	 public static final int DOWNLOAD_STATE_DOWNLOADING = 2;
	 public static final int DOWNLOAD_STATE_FINISH = 1;
	 public static final int DOWNLOAD_STATE_ERROR = 3;
	 public static final int DOWNLOAD_STATE_PAUSE = 4;
	 public static final int DOWNLOAD_STATE_CANCEL = 5;
	 *  */
	public int state = Constants.DOWNLOAD_STATE_IDLE;
	public Boolean isInstall = false;
	public Boolean isAutoInstall = false;
	public String url;
	public String downPath;
	public int downLength = 0;
	public int totalLength = 0;
	public HashMap<Integer, Integer> ranges;

	public File getDownloadFile() {
		return new File(downPath, name);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null){
			return false;
		}
		return this.hashCode() == o.hashCode();
	}

	@Override
	public int hashCode() {
		return url.hashCode()+downPath.hashCode()*31;
	}
}
