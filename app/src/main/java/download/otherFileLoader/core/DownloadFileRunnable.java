package download.otherFileLoader.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import download.otherFileLoader.listener.DownloadPercentListener;
import download.otherFileLoader.request.DownFile;

public class DownloadFileRunnable implements Runnable {
	private DownFile mDownFile;
	private File saveFile;
	private volatile boolean flag = false;
	private volatile HttpURLConnection http;
	private ApkLoader downloader;

	private DownloadPercentListener listener;

	public DownloadFileRunnable(DownFile mDownFile, File saveFile,
			ApkLoader downloader, DownloadPercentListener listener) {
		super();
		this.mDownFile = mDownFile;
		this.saveFile = saveFile;
		this.downloader = downloader;
		this.listener = listener;

	}

	public void cancel() {
		flag = false;
		if (http != null) {
			http.disconnect();
		}
	}

	public DownFile getmDownFile() {
		return mDownFile;
	}

	@Override
	public void run() {
		flag = true;
		mDownFile
				.setTotalLength(getContentLengthFormUrl(mDownFile.getDownUrl()));
		InputStream inStream = null;
		RandomAccessFile threadfile = null;
		// 未下载完成
		if (mDownFile.state != 1) {
			try {
				// 使用Get方式下载
				URL mUrl = new URL(mDownFile.getDownUrl());
				http = (HttpURLConnection) mUrl.openConnection();
				http.setConnectTimeout(5 * 1000);

				http.setRequestMethod("GET");
				http.setRequestProperty(
						"Accept",
						"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
				http.setRequestProperty("Accept-Language", "zh-CN");
				http.setRequestProperty("Referer", mDownFile.getDownUrl());
				http.setRequestProperty("Charset", "UTF-8");
				// 设置获取实体数据的范围
				http.setRequestProperty(
						"Range",
						"bytes=" + mDownFile.getDownLength() + "-"
								+ mDownFile.getTotalLength());
				http.setRequestProperty(
						"User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
				http.setRequestProperty("Connection", "Keep-Alive");
				try {
					inStream = http.getInputStream();
				} catch (Exception e) {
					if (mDownFile.getDownLength() > 0
							&& mDownFile.getDownLength() >= mDownFile
									.getTotalLength()) {
						flag = false;
						mDownFile.state = 1;
						if (listener != null) {
							listener.notify(mDownFile);
						}
					}
					e.printStackTrace();
				}
				byte[] buffer = new byte[1024];
				int offset = 0;
				threadfile = new RandomAccessFile(this.saveFile, "rwd");
				threadfile.seek(mDownFile.getDownLength());
				while (flag) {
					offset = inStream.read(buffer, 0, 1024);
					if (offset == -1) {
						flag = false;
						mDownFile.state = 1;
						if (listener != null) {
							listener.notify(mDownFile);
						}
						break;
					}
					if (offset >= 0) {
						threadfile.write(buffer, 0, offset);
						mDownFile.setDownLength(mDownFile.getDownLength()
								+ offset);
						offset = 0;
						mDownFile.state = 2;
						downloader.update(mDownFile);
						if (listener != null) {
							listener.notify(mDownFile);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				mDownFile.setDownLength(-1);
			} finally {
				try {
					if (threadfile != null) {
						threadfile.close();
					}
					if (inStream != null) {
						inStream.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 描述：获取网络文件的大小.
	 *
	 * @param Url
	 *            图片的网络路径
	 * @return int 网络文件的大小
	 */
	public int getContentLengthFormUrl(String Url) {
		int mContentLength = 0;
		try {
			URL url = new URL(Url);
			http = (HttpURLConnection) url
					.openConnection();
			http.setConnectTimeout(5 * 1000);
			http.setRequestMethod("GET");
			http
					.setRequestProperty(
							"Accept",
							"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			http.setRequestProperty("Accept-Language", "zh-CN");
			http.setRequestProperty("Referer", Url);
			http.setRequestProperty("Charset", "UTF-8");
			http
					.setRequestProperty(
							"User-Agent",
							"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			http.setRequestProperty("Connection", "Keep-Alive");
			http.connect();
			if (http.getResponseCode() == 200) {
				// 根据响应获取文件大小
				mContentLength = http.getContentLength();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mContentLength;
	}

}
