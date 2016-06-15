package download.otherFileLoader.request;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.UniqueCombine;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import download.otherFileLoader.listener.DownloadListener;
import download.utils.Util;

public class DownFile implements Serializable{

	private DownFile() {
	}

	public DownFile(String downUrl) {
		super();
		this.url = downUrl;
		state = 0;
		this.isCanceled = false;
		this.isPaused = false;
		this.isSuppurtRanger = false;
		this.isInstall = 0;
	}
	public DownloadListener listener;
	public volatile boolean isCanceled = false;
	public volatile boolean isPaused = false;
	public long lastNotifyTime;
	public String icon;
	public String name;
	public String description;
	public Boolean isSuppurtRanger = false;
	public String pakageName;
	/** 1表示已下载完成 0表示未开始下载 2表示已开始下载. */
	public int state;
	/**
	 * 1表示安装了0表示没安装
	 */
	public int isInstall;
	public Boolean isAutoInstall;
	@UniqueCombine(1)
	@NotNull
	public String url;
	
	/** 文件保存路径. */

	@UniqueCombine(1)
	@NotNull
	public String downPath;
	public int downLength;
	public int totalLength;
	public HashMap<Integer, Integer> ranges;

	public File getDownloadFile() {
		return new File(downPath, Util.md5(url));
	}

	@Override
	public boolean equals(Object o) {
		return o.equals(this);
	}

	@Override
	public int hashCode() {
		return url.hashCode()+downPath.hashCode()*31;
	}
}
