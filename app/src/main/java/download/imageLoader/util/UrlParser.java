package download.imageLoader.util;

import download.imageLoader.loader.UrlType;


//i.e. "http://site.com/image.png", "file:///mnt/sdcard/image.png" assets:test.png "drawable:"+R.drawable.common_logo,
public class UrlParser {
	public static UrlType getUrlType(String path) {
		if (path.contains("http:") || path.contains("https:")) {
			return UrlType.HTTP;
		} else if (path.contains("assets:")) {
			return UrlType.ASSETS;
		} else if (path.contains("drawable:")) {
			return UrlType.DRAWABLE;
		} else if (path.contains("file:")) {
			return UrlType.FILE;
		} else {
			return UrlType.UNKNOWN;
		}
	}

	public static Boolean isNetWork(String path) {
		return getUrlType(path) == UrlType.HTTP;
	}
}
