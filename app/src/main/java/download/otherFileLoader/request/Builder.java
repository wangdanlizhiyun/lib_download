package download.otherFileLoader.request;

import android.content.Context;
import android.text.TextUtils;

import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.listener.DownloadListener;
import download.utils.Util;


@SuppressWarnings("serial")
public class Builder {
	private Context context;
	private  Builder(){

	}
	public Builder(Context context) {
		this.context = context;
		this.isAutoInstall = false;

	}

	public Boolean isAutoInstall;

	public String downUrl;
	
	public String downPath;
	public DownloadListener listener;

	public Builder url(String url){
		this.downUrl = url;
		return this;
	}

	public Builder savePath(String path){
		this.downPath = path;
		return this;
	}
	public Builder listen(DownloadListener listener){
		this.listener = listener;
		return this;
	}

	public Builder autoInstanll(){
		this.isAutoInstall = true;
		return this;
	}
	private DownFile build(){
		DownFile downFile = new DownFile(downUrl);
		downFile.isAutoInstall = isAutoInstall;
		downFile.downPath = downPath;
		downFile.listener = listener;
		return  downFile;
	}
	public void download(){
		DownFile downFile = build();
		DownFileManager.getInstance(context).down(downFile);
	}
}
