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
import android.view.View;
import android.widget.ImageView;

import download.imageLoader.core.BmManager;
import download.imageLoader.core.Image;
import download.imageLoader.core.ImageLoader;
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
	public static final int SHAPE_RECTANGLE = 0,SHAPE_CYCLE = 1,SHAPE_ROUND = 2;
	private int SHAPE = SHAPE_RECTANGLE;
	private int boder_radius = 10;

	float scaleH = 1f;
	float scaleW = 1f;

	private Path mPath;

	private long nowTime,dur;
	private float mScale;

	private int mMeasuredMovieWidth;
	private int mMeasuredMovieHeight;

	private boolean mVisible = true;
	private float mLeft,mTop;
	private boolean isNeedSetPath = true;
	private Runnable mDelayShowGifRunnable;

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
		mDelayShowGifRunnable = new Runnable() {
			@Override
			public void run() {
				mVisible = true;
				setImageDrawable(null);
				requestLayout();
				notifyLayerType();
				invalidateView();
			}
		};
	}

	public PowerImageView setBorder(int color,float borderWidth){
		isNeedSetPath = true;
		this.mBorderColor = color;
		this.mBorderWidth = borderWidth;
		mBorderPaint.setColor(mBorderColor);
		mBorderPaint.setStrokeWidth(mBorderWidth);
		invalidateView();
		return this;
	}

	public PowerImageView circle(){
		isNeedSetPath = true;
		this.SHAPE = SHAPE_CYCLE;
		requestLayout();
		return this;
	}
	public PowerImageView rectangle(){
		isNeedSetPath = true;
		this.SHAPE = SHAPE_RECTANGLE;
		requestLayout();
		return this;
	}
	public PowerImageView round(int round){
		isNeedSetPath = true;
		this.SHAPE = SHAPE_ROUND;
		if (round > 0){
			boder_radius = round;
		}
		requestLayout();
		return this;
	}
	public Boolean isBlur;

	public PowerImageView blur(Boolean b){
		isBlur = b;
		return this;
	}
	public void bind(String path){
		Image.with(getContext()).blur(isBlur).load(path).into(this);
	}

	public void setMovie(Movie movie) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("only run on ui thread");
		}
		this.mMovie = movie;
		mVisible = false;
		removeCallbacks(mDelayShowGifRunnable);
		postDelayed(mDelayShowGifRunnable,30);
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
		this.clearAnimation();
		this.mMovie = null;
		this.setImageDrawable(null);
		ImageLoader.getInstance().cancelOldTask(this);
		removeCallbacks(mDelayShowGifRunnable);
		super.onDetachedFromWindow();
	}

	public void setMovieTime(int time) {
		mCurrentAnimationTime = time;
		invalidate();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		float maximumWidth = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
		int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		float maximumHeight = MeasureSpec.getSize(heightMeasureSpec)-getPaddingTop()-getPaddingBottom();
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
			//强行完全显示
			mMeasuredMovieWidth = (int) (movieWidth * mScale);
			mMeasuredMovieHeight = (int) (movieHeight * mScale);
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		} else {
			super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		}
	}
	private void notifySetedBitmap(){
		notifyLayerType();
		invalidateView();
		removeCallbacks(mDelayShowGifRunnable);
	}
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		notifySetedBitmap();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		notifySetedBitmap();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		notifySetedBitmap();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		notifySetedBitmap();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mVisible = getVisibility() == View.VISIBLE;

		if (mMovie != null && getDrawable() == null) {
			mLeft = (getWidth() - mMeasuredMovieWidth - getPaddingLeft() - getPaddingRight()) / 2f + getPaddingLeft();
			mTop = (getHeight() - mMeasuredMovieHeight - getPaddingTop() - getPaddingBottom()) / 2f + getPaddingTop();

		}else {
			mLeft = getPaddingLeft();
			mTop = getPaddingTop();
		}
		isNeedSetPath = true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onDraw(Canvas canvas) {
		if (!mVisible){
			return;
		}
		if (mMovie != null && getDrawable() == null) {
			drawMovieFrame(canvas);
			postDelayed(new Runnable() {
				@Override
				public void run() {
					invalidateView();
				}
			},60);
		}else {
			//直接修改ImageView源代码
			clipDrawable(canvas);
			super.onDraw(canvas);
			if (mBorderWidth > 0){
				canvas.drawPath(mPath,mBorderPaint);
			}
			canvas.restore();
		}
	}
	private int getShowWidth(){
		if (mMovie != null && getDrawable() == null) {
			return mMeasuredMovieWidth-getPaddingLeft()-getPaddingRight();
		}else {
			return getWidth()-getPaddingLeft()-getPaddingRight();
		}
	}
	private int getShowHeight(){
		if (mMovie != null && getDrawable() == null) {
			return mMeasuredMovieHeight-getPaddingLeft()-getPaddingRight();
		}else {
			return getHeight()-getPaddingTop()-getPaddingBottom();
		}
	}
	private void clipDrawable(Canvas canvas) {
		canvas.save();
		canvas.translate(mLeft, mTop);
		if (!isNeedSetPath){
			canvas.clipPath(mPath, Region.Op.REPLACE);
			return;
		}
		mPath.reset();
		RectF rect = new RectF(getPaddingLeft(),getPaddingTop(), getShowWidth(), getShowHeight());
		canvas.clipPath(mPath);
		switch (SHAPE){
			case SHAPE_CYCLE:
				mPath.addCircle(rect.centerX(), rect.centerY(),
						Math.min(getWidth()/2-getPaddingLeft()-getPaddingRight(), getHeight()/2-getPaddingTop()-getPaddingBottom()), Path.Direction.CCW);
				break;
			case SHAPE_ROUND:
				mPath.addRoundRect(rect, new float[]{boder_radius, boder_radius, boder_radius, boder_radius,boder_radius, boder_radius, boder_radius, boder_radius}, Path.Direction.CCW);
				break;
			case SHAPE_RECTANGLE:
				mPath.addRect(rect, Path.Direction.CCW);
				break;
		}
		canvas.clipPath(mPath, Region.Op.REPLACE);
	}

	private void clipGif(Canvas canvas){
		canvas.translate(mLeft/mScale,mTop /mScale);
		if (!isNeedSetPath){
			canvas.clipPath(mPath, Region.Op.REPLACE);
			return;
		}
		mPath.reset();
		RectF rect = new RectF(getPaddingLeft()/2, getPaddingTop()/2, (int) (getShowWidth()/mScale), (int) (getShowHeight()/mScale));
		canvas.clipPath(mPath);
		switch (SHAPE) {
			case SHAPE_CYCLE:
				mPath.addCircle(getShowWidth() / 2 / mScale, getShowHeight() / 2 / mScale,
						Math.min(getShowWidth(), getShowHeight()) / 2 / mScale * Math.max(scaleH,scaleW) / Math.min(scaleH,scaleW), Path.Direction.CCW);
				break;
			case SHAPE_ROUND://纠结是该让gif圆角化还是view圆角化，貌似让gif圆角好看点。但是按理是该让view圆角。当填充方式是centerscrop时两者效果一样
				mPath.addRoundRect(rect, new float[]{boder_radius / mScale, boder_radius / mScale, boder_radius / mScale, boder_radius / mScale, boder_radius / mScale, boder_radius/mScale, boder_radius/mScale, boder_radius/mScale}, Path.Direction.CCW);
				break;
			case SHAPE_RECTANGLE:
				mPath.addRect(rect,Path.Direction.CCW);
				break;
		}
		canvas.clipPath(mPath, Region.Op.REPLACE);
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
		mMovie.draw(canvas, getPaddingLeft()/2, getPaddingTop()/2, mBitmapPaint);
		if (mBorderWidth > 0){
			canvas.drawPath(mPath,mBorderPaint);
		}
		canvas.restore();

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
