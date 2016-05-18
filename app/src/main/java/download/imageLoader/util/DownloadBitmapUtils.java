package download.imageLoader.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import download.imageLoader.cache.DiskLruCache;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ImageSizeUtil.ImageSize;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.View;

public class DownloadBitmapUtils {
	public static final int bigSize = 10 * 1024;

	public interface LoadListener {
		public abstract void getMorePrecent();
	}
	public static void downloadBitmapByUrl(BitmapRequest request,
			DiskLruCache diskLruCache, BackListener listener) {
			if (diskLruCache == null) {
				return ;
			}
			final String key = Util.md5(request.path);
			DiskLruCache.Editor editor;
			try {
				editor = diskLruCache.edit(key);
				if (editor != null) {
					OutputStream outputStream = editor.newOutputStream(0);
					BufferedOutputStream out = null;
					BufferedInputStream in = null;
					HttpURLConnection conn = null;
					int sum = 0;
					int p = 1;
					try {
						URL url = new URL(request.path);
						conn = (HttpURLConnection) url.openConnection();
						request.totalSize = conn.getContentLength();
						in = new BufferedInputStream(conn.getInputStream(), 8 * 1024);
						out = new BufferedOutputStream(outputStream, 8 * 1024);
						int b = 0;
						while ((b = in.read()) != -1) {
							out.write(b);
								sum++;
								request.percent = (int) (sum * 100 / request.totalSize);
								if (request.percent >= p && p <= 100) {
									p += 1;
									listener.onProcess(request.percent);
								}
						}
						editor.commit();
					} catch (Exception e) {
						e.printStackTrace();
						editor.abort();
					} finally {
						Util.close(conn, out, in);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				diskLruCache.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}


	public static Bitmap downloadImgByUrl(BitmapRequest request) {
		FileOutputStream fos = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(request.path);
			conn = (HttpURLConnection) url.openConnection();
			is = new BufferedInputStream(conn.getInputStream());
			is.mark(is.available());
			Options opts = new Options();
			opts.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
			// 获取imageview想要显示的宽和高
			ImageSizeUtil.getImageViewSize(request);
			opts.inSampleSize = ImageSizeUtil.caculateInSampleSize(opts,
					request.width, request.height);
			opts.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeStream(is, null, opts);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Util.close(conn, fos, is);
		}

		return null;
	}

	public static Bitmap loadImageFromAssets(BitmapRequest request) {
		String name = request.path.substring(9);
		InputStream is = null;
		try {
			is = request.view.getContext().getAssets().open(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (is == null) {
			return null;
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		return bm;
	}

	public static Bitmap loadImageFromLocal(final String path, BitmapRequest request) {
		if (!new File(path).exists()) {
			return null;
		}
		ImageSizeUtil.getImageViewSize(request);
		Bitmap bm;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				request.width, request.height);
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, options);
		return bm;
	}

	public static Bitmap loadImageFromDrawable(BitmapRequest request) {
		int id = 0;
		try {
			id = Integer.parseInt(request.path.substring(11));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if (id == 0) {
			return null;
		}
		Bitmap bm;
		ImageSizeUtil.getImageViewSize(request);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(request.view.getResources(), id, options);
		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				request.width, request.height);
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeResource(request.view.getResources(), id, options);
		return bm;
	}
}
