package download.otherFileLoader.request;

import android.content.Context;

import java.io.Serializable;

import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.listener.DownloadListener;


@SuppressWarnings("serial")
public class DownBuilder {
	private Context context;
	private DownBuilder(){

	}
	public DownBuilder(Context context) {
		this.context = context;
		this.isAutoInstall = false;
	}

	public Boolean isAutoInstall;

	public String downUrl;
	
	public String downPath;
	public DownloadListener listener;

	public DownBuilder url(String url){
		this.downUrl = url;
		return this;
	}

	public DownBuilder savePath(String path){
		this.downPath = path;
		return this;
	}
	public DownBuilder listen(DownloadListener listener){
		this.listener = listener;
		return this;
	}

	public DownBuilder autoInstanll(){
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
