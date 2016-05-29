package download.imageLoader.util;

import java.lang.reflect.Field;

import download.imageLoader.request.BitmapRequest;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/**
 * http://blog.csdn.net/lmj623565791/article/details/41874561
 * 
 * @author zhy
 *
 */
public class ImageSizeUtil {
	/**
	 * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
	 *
	 */
	public static int caculateInSampleSize(Options options, int reqWidth,
			int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;

		int inSampleSize = 1;

		if (width > reqWidth || height > reqHeight) {
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);

			inSampleSize = Math.max(widthRadio, heightRadio);
		}

		return inSampleSize;
	}

	/**
	 * 根据ImageView获适当的压缩的宽和高
	 *
	 */
	public static void getImageViewSize(BitmapRequest request) {
		if (request.width > 0 && request.height > 0) {
			return;
		}
		if (request.view == null || request.view.get() == null) {
			request.width = 300;
			request.height = 300;
			return;
		}

		LayoutParams lp = request.view.get().getLayoutParams();

		int width = request.view.get().getWidth() - request.view.get().getPaddingLeft() - request.view.get().getPaddingRight();// 获取imageview的实际宽度
		if (width <= 0) {
			width = lp.width - request.view.get().getPaddingLeft() - request.view.get().getPaddingRight();// 获取imageview在layout中声明的宽度
		}
		if (width <= 0) {
			// width = imageView.getMaxWidth();// 检查最大值
			width = getImageViewFieldValue(request.view, "mMaxWidth") - request.view.get().getPaddingTop() - request.view.get().getPaddingBottom();
		}
		if (width <= 0) {
			width = 300;
		}

		int height = request.view.get().getHeight() - request.view.get().getPaddingTop() - request.view.get().getPaddingBottom();// 获取imageview的实际高度
		if (height <= 0) {
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		if (height <= 0) {
			height = getImageViewFieldValue(request.view, "mMaxHeight") - request.view.get().getPaddingTop() - request.view.get().getPaddingBottom();// 检查最大值
		}
		if (height <= 0) {
			height = 300;
		}
		request.width = width;
		request.height = height;

	}

	public static class ImageSize {
		public int width;
		public int height;
	}

	/**
	 * 通过反射获取imageview的某个属性值
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = field.getInt(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
		}
		return value;

	}

}
