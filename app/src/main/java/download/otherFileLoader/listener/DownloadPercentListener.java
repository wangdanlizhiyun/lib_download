package download.otherFileLoader.listener;

import download.otherFileLoader.request.DownFile;


public interface DownloadPercentListener {
	public long lastTime = 0;
	public void notify(DownFile downFile);
}
