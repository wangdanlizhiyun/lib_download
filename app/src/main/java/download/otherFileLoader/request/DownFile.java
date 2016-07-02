package download.otherFileLoader.request;

import java.io.Serializable;
import java.util.HashMap;

import download.otherFileLoader.listener.DownloadListener;
import download.utils.Util;

public class DownFile implements Serializable{


	public DownFile() {
	}

	public DownFile(String downUrl) {
		super();
		this.url = downUrl;
		this.name = Util.getNameFromUrl(url);
		this.state = DownloadStatus.IDLE;
		this.downLength = 0;
		this.totalLength = 0;
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
	public Boolean isSuppurtRanger;
	public String pakageName;
	public DownloadStatus state = DownloadStatus.IDLE;
	public Boolean isInstall;
	public Boolean isAutoInstall;
	public String url;
	public String downPath;
	public int downLength;
	public int totalLength;
	public HashMap<Integer, Integer> ranges;
	public enum DownloadStatus{
		IDLE(0,"空闲"),DOWNLOADING(2,"下载中"),FINISH(1,"完成"),ERROR(3,"异常"),PAUSE(4,"暂停"),CANCEL(5,"取消"),WAITING(6,"等待");
		public int value;
		public String name;
		private DownloadStatus(int value,String name){
			this.name = name;
			this.value = value;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null){
			return false;
		}
		if (!(o instanceof DownFile)){
			return false;
		}
		DownFile other = (DownFile) o;
		return this.url.equals(other.url) && this.downPath.equals(other.downPath);
	}
}
