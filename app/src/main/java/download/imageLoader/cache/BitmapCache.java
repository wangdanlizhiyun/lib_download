package download.imageLoader.cache;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil;
import download.utils.Util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.StatFs;
import android.os.Build.VERSION_CODES;
import android.util.LruCache;

@SuppressLint("NewApi")
public class BitmapCache {
	private LruCache<String, BitmapDrawable> mMemoryBitmapLruCache;
	private LruCache<String, Movie> mMemoryMovieLruCache;
	private LruCache<String, BitmapDrawable> mPercentLruCache;
	private DiskLruCache mDiskLruCache = null;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 60;
	private class Size{
		public int width;
		public int height;
		public Size(int w,int h){
			this.width = w;
			this.height = h;
		}
	}
	private HashMap<String, Size> mHistoryMaxSize = new HashMap<String, Size>();

	

	@SuppressLint("NewApi")
	public BitmapCache() {
		super();
		// 获取我们应用的最大可用内存
		int maxMemory = Math.min(
				(int) Runtime.getRuntime().maxMemory()  / 8, 30 * 1024 * 1024);
		mMemoryBitmapLruCache = new LruCache<String, BitmapDrawable>(maxMemory) {
			@Override
			protected int sizeOf(String key, BitmapDrawable value) {
				return getBitmapByteSize(value);
			}
		};
		mMemoryMovieLruCache = new LruCache<String, Movie>(10){
			@Override
			protected int sizeOf(String key, Movie value) {
				return 1;
			}
		};
		mPercentLruCache = new LruCache<String, BitmapDrawable>(maxMemory / 5) {
			@Override
			protected int sizeOf(String key, BitmapDrawable value) {
				return getBitmapByteSize(value);
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

	public void addMemoryBitmap(BitmapRequest request) {
		String key = Util.md5(request.path);
		if (request.bitmap != null){
			//保存使用过的尽可能的最大值
			Size oldSize = mHistoryMaxSize.get(request.path);
			Size newSize = new Size(request.width,request.height);
			if (oldSize == null || ( newSize.width * newSize.height > oldSize.width * oldSize.height )){
				mHistoryMaxSize.put(request.path,newSize);
			}
			mMemoryBitmapLruCache.put(key, request.bitmap);
		}
		if (request.movie != null){
			mMemoryMovieLruCache.put(key,request.movie);
		}
	}

	public void addPercentBitmap(String path, BitmapDrawable bm) {
		String key = Util.md5(path);
		mPercentLruCache.remove(key);
		if (bm != null){
			mPercentLruCache.put(key, bm);
		}
	}

	public void getMemoryCache(BitmapRequest request) {
		String key = Util.md5(request.path);
		ImageSizeUtil.getImageViewSize(request);
		request.movie = mMemoryMovieLruCache.get(key);
		if (request.checkIfNeedAsyncLoad()){
			request.bitmap = mMemoryBitmapLruCache.get(key);

			Size oldSize = mHistoryMaxSize.get(request.path);
			if (oldSize != null && request.view != null && request.bitmap != null && (request.width > oldSize.width || request.height > oldSize.height)) {
				request.bitmap = null;
			}
		}

	}

	public DiskLruCache getmDiskLruCacheBitmap() {
		return mDiskLruCache;
	}


	public void getDiskCacheBitmap(BitmapRequest request) {
		if (mDiskLruCache == null) {
			return ;
		}
		String key = Util.md5(request.path);
		try {
			DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
			if (snapShot != null) {
				loadImageFromLocal(diskCacheDir.getAbsolutePath()
						+ File.separator + key + ".0", request);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadImageFromLocal(String path, BitmapRequest request) {
		if (request.view == null || request.view.get() == null){
			return;
		}
		if (!new File(path).exists()) {
			return ;
		}
		ImageSizeUtil.getImageViewSize(request);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				request.width, request.height);
		options.inJustDecodeBounds = false;
		if (request.totalSize > BitmapRequest.bigSize && request.percent > 0 && request.percent < 100){//大图获取百分比
			request.bitmap = new BitmapDrawable(BitmapFactory.decodeFile(path, options));
		}else {
			try{
				request.movie = Movie.decodeStream(new FileInputStream(new File(path)));
			}catch (Exception e){

			}
			if (request.checkIfNeedAsyncLoad()){
				request.bitmap = new BitmapDrawable(BitmapFactory.decodeFile(path, options));
			}
		}

	}

	public Boolean hasDiskBm(String path) {
		String key = Util.md5(path);
		File file = new File(diskCacheDir.getAbsolutePath()+ File.separator + key + ".0");
		return file.exists() && file.length() > 0;
	}

	public void clearMemory(){
		mMemoryBitmapLruCache.evictAll();
		mPercentLruCache.evictAll();
		mMemoryMovieLruCache.evictAll();
	}
	public void clearDiskMemory(){
		try {
			if (mDiskLruCache != null){
				mDiskLruCache.delete();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private int getBitmapByteSize(BitmapDrawable bitmap) {
		// The return value of getAllocationByteCount silently changes for recycled bitmaps from the
		// internal buffer size to row bytes * height. To avoid random inconsistencies in caches, we
		// instead assert here.
		if (bitmap.getBitmap().isRecycled()) {
			throw new IllegalStateException("Cannot obtain size for recycled Bitmap: " + bitmap
					+ "[" + bitmap.getBitmap().getWidth() + "x" + bitmap.getBitmap().getHeight() + "] " + bitmap.getBitmap().getConfig());
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
			try {
				return bitmap.getBitmap().getAllocationByteCount();
			} catch (NullPointerException e) {
				// Do nothing.
			}
		}
		return bitmap.getBitmap().getHeight() * bitmap.getBitmap().getRowBytes();
	}
}
