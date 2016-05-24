package download.imageLoader.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import download.imageLoader.cache.DiskLruCache;
import download.imageLoader.listener.BackListener;
import download.imageLoader.request.BitmapRequest;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;

public class DownloadBitmapUtils {
	public static final int bigSize = 1024 * 1024;
	public static void downloadBitmapToDisk(BitmapRequest request,
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
								if (request.percent >= p) {
									p += 1;
									if (request.percent < 100){
										listener.onProcess(request.percent);
									}
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


	public static void downloadImgByUrl(BitmapRequest request) {
		FileOutputStream fos = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(request.path);
			conn = (HttpURLConnection) url.openConnection();
			is = new BufferedInputStream(conn.getInputStream());
			is.mark(is.available());
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			ImageSizeUtil.getImageViewSize(request);
			options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
					request.width, request.height);
			options.inJustDecodeBounds = false;
			request.movie = Movie.decodeStream(is);
			if (request.checkIfNeedAsyncLoad()){
				request.bitmap = new BitmapDrawable(request.view.get().getResources(),BitmapFactory.decodeStream(is, null, options));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Util.close(conn, fos, is);
		}
	}

	public static void loadImageFromAssets(BitmapRequest request) {
		String name = request.path.substring(9);
		InputStream is = null;
		try {
			if (request.view.get() == null){
				return;
			}
			is = request.view.get().getContext().getAssets().open(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (is == null) {
			return;
		}
		ImageSizeUtil.getImageViewSize(request);
		request.movie = Movie.decodeStream(is);
		if (request.checkIfNeedAsyncLoad()){
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
					request.width, request.height);
			options.inJustDecodeBounds = false;
			request.bitmap = new BitmapDrawable(request.view.get().getResources(),BitmapFactory.decodeStream(is, null, options));
		}
	}


	public static void loadImageFromLocal(final String path, BitmapRequest request) {
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
		if (request.totalSize > DownloadBitmapUtils.bigSize && request.percent > 0 && request.percent < 100){//大图获取百分比
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

	public static void loadImageFromDrawable(BitmapRequest request) {
		int id = 0;
		try {
			id = Integer.parseInt(request.path.substring(11));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		if (id == 0) {
			return;
		}
		ImageSizeUtil.getImageViewSize(request);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		if (request.view.get() == null){
			return;
		}
		BitmapFactory.decodeResource(request.view.get().getResources(), id, options);
		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				request.width, request.height);
		options.inJustDecodeBounds = false;
		try{
			request.movie = Movie.decodeStream(request.view.get().getResources().openRawResource(id));
		}catch (Exception e){

		}
		if (request.checkIfNeedAsyncLoad()){
			request.bitmap = new BitmapDrawable(request.view.get().getResources(),BitmapFactory.decodeResource(request.view.get().getResources(), id, options));
		}
	}
}
