package download.imageLoader.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import download.imageLoader.core.BmLoader;
import download.imageLoader.listener.CustomDisplayMethod;


public class PowerImageView extends ImageView {
	private final int DEFAULT_MOVIEW_DURATION = 1000;
	private Movie mMovie;
	private long mMovieStart;
	private int mCurrentAnimationTime = 0;
	private final Paint mBitmapPaint = new Paint();
	private Paint mBorderPaint = new Paint();
	private float mBorderWidth = 0;
	private int mBorderColor = Color.BLACK;
	private final int TYPE_RECTANGLE = 0,TYPE_CYCLE = 1,TYPE_ROUND = 2;
	private int type = TYPE_RECTANGLE;
	private int boder_radius = 10;

	float scaleH = 1f;
	float scaleW = 1f;

	private Path mPath;

	private long nowTime,dur;
	private float mScale;

	private int mMeasuredMovieWidth;
	private int mMeasuredMovieHeight;

	private boolean mVisible = true;

	public PowerImageView(Context context) {
		this(context, null);
	}

	public PowerImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PowerImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mBitmapPaint.setAntiAlias(true);
		mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);
		mPath = new Path();
		setBackgroundColor(Color.TRANSPARENT);
		setPadding(0,0,0,0);
	}

	/**
	 * 禁用padding，因为意义不大，请用margin代替效果
	 */
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		super.setPadding(0, 0, 0, 0);
	}

	public PowerImageView setBorder(int color,float borderWidth){
		this.mBorderColor = color;
		this.mBorderWidth = borderWidth;
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);
		invalidateView();
		return this;
	}

	public PowerImageView setCircle(){
		this.type = TYPE_CYCLE;
		requestLayout();
		return this;
	}
	public PowerImageView setRectangle(){
		this.type = TYPE_RECTANGLE;
		requestLayout();
		return this;
	}
	public PowerImageView setRound(int round){
		this.type = TYPE_ROUND;
		if (round > 0){
			boder_radius = round;
		}
		requestLayout();
		return this;
	}
	public void bind(String path){
		BmLoader.load(path, this);
	}
	public void bind(String path,CustomDisplayMethod customDisplayMethod){
		BmLoader.load(path, this, customDisplayMethod);
	}

	public void setMovie(Movie movie) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("only run on ui thread");
		}
		notifyLayerType();
		this.mMovie = movie;
		//至关重要的设置
		setImageDrawable(null);
		requestLayout();
	}

	private void notifyLayerType() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mMovie != null && getDrawable() == null){
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}else {
				setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
		}
	}


	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.clearAnimation();
		this.mMovie = null;
		this.setImageDrawable(null);
	}

	public void setMovieTime(int time) {
		mCurrentAnimationTime = time;
		invalidate();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		float maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
		int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		float maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (mMovie != null && getDrawable() == null){
			float movieWidth = mMovie.width();
			float movieHeight = mMovie.height();
			if (measureModeWidth == MeasureSpec.AT_MOST && measureModeHeight == MeasureSpec.AT_MOST){
				if (movieHeight > maximumHeight && maximumHeight > 0)
					scaleH = movieHeight/maximumHeight;
				if (movieWidth > maximumWidth && maximumWidth > 0)
					scaleW = movieWidth/maximumWidth;
			}else {
				if (maximumHeight > 0)
					scaleH = movieHeight/maximumHeight;
				if (maximumWidth > 0)
					scaleW = movieWidth/maximumWidth;
			}

			if (Math.max(scaleH,scaleW) > 0){
				mScale = 1f/Math.max(scaleH,scaleW);
			}else {
				mScale = 1.0f;
			}
			mMeasuredMovieWidth = (int) (movieWidth * mScale);
			mMeasuredMovieHeight = (int) (movieHeight * mScale);
			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
		} else {
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		}
	}
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		notifyLayerType();
		invalidateView();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		notifyLayerType();
		invalidateView();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		notifyLayerType();
		invalidateView();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		notifyLayerType();
		invalidateView();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mVisible = getVisibility() == View.VISIBLE;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onDraw(Canvas canvas) {
		if (!mVisible){
			return;
		}
		if (mMovie != null && getDrawable() == null) {
			drawMovieFrame(canvas);
			invalidateView();
		}else {
			//直接修改ImageView源代码
			clipDrawable(canvas);
			super.onDraw(canvas);
			if (mBorderWidth > 0){
				canvas.drawPath(mPath,mBorderPaint);
			}
//			if (getDrawable() == null) {
//				return; // couldn't resolve the URI
//			}
//
//			if (getDrawable().getIntrinsicWidth() <= 0 || getDrawable().getIntrinsicHeight() <= 0) {
//				return;     // nothing to draw (empty bounds)
//			}
//
//			if (getImageMatrix() == null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
//				clipDrawable(canvas);
//				getDrawable().draw(canvas);
//			} else {
//				int saveCount = canvas.getSaveCount();
//				canvas.save();
//
//				if (getCropToPadding()) {
//					final int scrollX = getScrollX();
//					final int scrollY = getScrollY();
//					canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
//							scrollX + getRight() - mLeft - getPaddingRight(),
//							scrollY + getBottom() - mTop - getPaddingBottom());
//				}
//
//				clipDrawable(canvas);
//
//				canvas.translate(getPaddingLeft(), getPaddingTop());
//
//				if (getImageMatrix() != null) {
//					canvas.concat(getImageMatrix());
//				}
//
//				getDrawable().draw(canvas);
//				canvas.restoreToCount(saveCount);
//			}
		}
	}
	private void clipDrawable(Canvas canvas){
		mPath.reset();
		RectF rect = new RectF(0,0, (int) (getWidth()), (int) (getHeight()));
		switch (type){
			case TYPE_CYCLE:
				canvas.clipPath(mPath);
				mPath.addCircle(getWidth() / 2 , getHeight() / 2 ,
						Math.min(getWidth(), getHeight()) / 2 , Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_ROUND:
				canvas.clipPath(mPath);
				mPath.addRoundRect(rect, new float[]{boder_radius, boder_radius, boder_radius, boder_radius,boder_radius, boder_radius, boder_radius, boder_radius}, Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_RECTANGLE:
				mPath.addRect(rect,Path.Direction.CCW);

				break;
		}
	}


	private void invalidateView() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			postInvalidateOnAnimation();
			invalidate();
		} else {
			invalidate();
		}
	}

	private void drawMovieFrame(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(mScale, mScale);
		clipGif(canvas);
		updateTime();
		mMovie.setTime(mCurrentAnimationTime);
		mMovie.draw(canvas, 0, 0, mBitmapPaint);
		if (mBorderWidth > 0){
			canvas.drawPath(mPath,mBorderPaint);
		}
		canvas.restore();

	}

	private void clipGif(Canvas canvas){
		mPath.reset();
		RectF rect = new RectF(0,0, (int) (getWidth()/mScale), (int) (getHeight()/mScale));
		switch (type){
			case TYPE_CYCLE:
				canvas.clipPath(mPath);
				mPath.addCircle(getWidth() / 2 / mScale, getHeight() / 2 /mScale,
						Math.min(getWidth(), getHeight()) / 2 / mScale * Math.max(scaleH,scaleW) / Math.min(scaleH,scaleW), Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_ROUND://纠结是该让gif圆角化还是view圆角化，貌似让gif圆角好看点。但是按理是该让view圆角。当填充方式是centerscrop时两者效果一样
				canvas.clipPath(mPath);
				mPath.addRoundRect(rect, new float[]{boder_radius/mScale, boder_radius/mScale, boder_radius/mScale, boder_radius/mScale,boder_radius/mScale, boder_radius/mScale, boder_radius/mScale, boder_radius/mScale}, Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_RECTANGLE:
				mPath.addRect(rect,Path.Direction.CCW);
				break;
		}
	}


	private void updateTime(){
		nowTime = android.os.SystemClock.uptimeMillis();
		if (mMovieStart == 0) {
			mMovieStart = nowTime;
		}
		dur = mMovie.duration();
		if (dur == 0) {
			dur = DEFAULT_MOVIEW_DURATION;
		}
		mCurrentAnimationTime = (int) ((nowTime - mMovieStart) % dur);
	}

	@SuppressLint("NewApi")
	@Override
	public void onScreenStateChanged(int screenState) {
		super.onScreenStateChanged(screenState);
		mVisible = screenState == SCREEN_STATE_ON;
		invalidateView();
	}

	@SuppressLint("NewApi")
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}
}
