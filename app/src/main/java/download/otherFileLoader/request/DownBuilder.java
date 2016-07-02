package download.otherFileLoader.request;

import android.content.Context;
import android.text.TextUtils;

import java.io.Serializable;

import download.otherFileLoader.db.DownFileManager;
import download.otherFileLoader.listener.DownloadListener;
import download.utils.Util;


@SuppressWarnings("serial")
public class DownBuilder {
	private Context context;
	private DownBuilder(){

	}
	public DownBuilder(Context context) {
		this.context = context;
		this.isAutoInstall = false;
		this.downPath = Util.getDiskCacheDir(context, "downFile").getAbsolutePath();
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
		if (!TextUtils.isEmpty(path)){
			this.downPath = path;
		}
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
	public DownFile build(){
		DownFile downFile = new DownFile(downUrl);
		downFile.isAutoInstall = isAutoInstall;
		downFile.downPath = downPath;
		downFile.listener = listener;
		downFile.isInstall = isAutoInstall;
		return  downFile;
	}
	public void download(){
		DownFile downFile = build();
		DownFileManager.getInstance(context).down(downFile);
	}
}
