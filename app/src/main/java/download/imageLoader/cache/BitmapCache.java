package download.imageLoader.cache;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.DownloadBitmapUtils;
import download.imageLoader.util.ImageSizeUtil;
import download.imageLoader.util.Util;
import download.imageLoader.util.ImageSizeUtil.ImageSize;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StatFs;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.LruCache;
import android.view.View;

@SuppressLint("NewApi")
public class BitmapCache {
	private LruCache<String, Bitmap> mMemoryLruCache;
	private LruCache<String, Bitmap> mPercentLruCache;
	private DiskLruCache mDiskLruCache = null;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;

	

	@SuppressLint("NewApi")
	public BitmapCache() {
		super();
		// 获取我们应用的最大可用内存
		int maxMemory = Math.min(
				(int) Runtime.getRuntime().maxMemory() / 1024 / 8, 10 * 1024);
		mMemoryLruCache = new LruCache<String, Bitmap>(maxMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return Util.getBitmapByteSize(value) / 1024;
			}
		};
		mPercentLruCache = new LruCache<String, Bitmap>(maxMemory / 5) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return Util.getBitmapByteSize(value) / 1024;
			}
		};
	}

	private Boolean isSetted = false;
	private File diskCacheDir;

	public void setDiskLruCache(Context context) {
		if (isSetted) {
			isSetted = true;
			return;
		}
		if (mDiskLruCache == null) {
			try {
				diskCacheDir = Util.getDiskCacheDir(context, "bitmap");
				if (!diskCacheDir.exists()) {
					diskCacheDir.mkdirs();
				}
		        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
		        	mDiskLruCache = DiskLruCache.open(diskCacheDir,
		        			1, 1, DISK_CACHE_SIZE);
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

    @SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }
    
	public void addMemoryBitmap(String path, Bitmap bm) {
		if (bm != null){
			String key = Util.md5(path);
			mMemoryLruCache.put(key, bm);
		}
	}

	public void addPercentBitmap(String path, Bitmap bm) {
		if (bm != null){
			String key = Util.md5(path);
			mPercentLruCache.put(key, bm);
		}
	}

	public Bitmap getMemoryCacheBitmap(BitmapRequest request) {
		String key = Util.md5(request.path);
		ImageSizeUtil.getImageViewSize(request);
		Bitmap bm = null;
		if (bm == null) {
			bm = mMemoryLruCache.get(key);
		}
		if (bm != null && (request.width > bm.getWidth() || request.height > bm.getHeight())) {
			bm = null;
		}
		return bm;
	}

	public DiskLruCache getmDiskLruCacheBitmap() {
		return mDiskLruCache;
	}
	public Bitmap getBitmap(BitmapRequest request){
		Bitmap bm = null;
		bm = getMemoryCacheBitmap(request);
		if (bm == null) {
			bm = getDiskCacheBitmap(request);
		}
		return bm;
	}

	public Bitmap getDiskCacheBitmap(BitmapRequest request) {
		if (mDiskLruCache == null) {
			return null;
		}
		Bitmap bm = null;
		String key = Util.md5(request.path);
		 try {
		 DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
		 if (snapShot != null) {
			 
			bm = DownloadBitmapUtils.loadImageFromLocal(diskCacheDir.getAbsolutePath()
					 + File.separator + key + ".0", request);
		 }
		 } catch (IOException e) {
		 e.printStackTrace();
		 }
		return bm;
	}
	public Boolean hasDiskBm(String path) {
		String key = Util.md5(path);
		File file = new File(diskCacheDir.getAbsolutePath()+ File.separator + key + ".0");
		return file.exists() && file.length() > 0;
	}
}
