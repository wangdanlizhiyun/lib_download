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
	private float mLeft,mTop;

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
		this.clearAnimation();
		this.mMovie = null;
		this.setImageDrawable(null);
		ImageLoader.getInstance().cancelOldTask(this);
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
//			setMeasuredDimension(mMeasuredMovieWidth+getPaddingLeft()+getPaddingRight(), mMeasuredMovieHeight+getPaddingTop()+getPaddingBottom());
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

		if (mMovie != null && getDrawable() == null) {
			mLeft = (getWidth() - mMeasuredMovieWidth - getPaddingLeft() - getPaddingRight()) / 2f + getPaddingLeft();
			mTop = (getHeight() - mMeasuredMovieHeight - getPaddingTop() - getPaddingBottom()) / 2f + getPaddingTop();

		}else {
			mLeft = getPaddingLeft();
			mTop = getPaddingTop();
		}
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
		mPath.reset();
		RectF rect = new RectF(getPaddingLeft(),getPaddingTop(), getShowWidth(), getShowHeight());
		canvas.clipPath(mPath);
		switch (type){
			case TYPE_CYCLE:

				mPath.addCircle(rect.centerX(), rect.centerY(),
						Math.min(getWidth()/2-getPaddingLeft()-getPaddingRight(), getHeight()/2-getPaddingTop()-getPaddingBottom()), Path.Direction.CCW);
				break;
			case TYPE_ROUND:
				mPath.addRoundRect(rect, new float[]{boder_radius, boder_radius, boder_radius, boder_radius,boder_radius, boder_radius, boder_radius, boder_radius}, Path.Direction.CCW);
				break;
			case TYPE_RECTANGLE:
				mPath.addRect(rect, Path.Direction.CCW);
				break;
		}
		canvas.clipPath(mPath, Region.Op.REPLACE);
	}

	private void clipGif(Canvas canvas){
		canvas.translate(mLeft/mScale,mTop/mScale);
		mPath.reset();
		RectF rect = new RectF(getPaddingLeft()/2, getPaddingTop()/2, (int) (getShowWidth()/mScale), (int) (getShowHeight()/mScale));
		canvas.clipPath(mPath);
		switch (type){
			case TYPE_CYCLE:
				mPath.addCircle(getShowWidth() / 2 / mScale, getShowHeight() / 2 /mScale,
						Math.min(getShowWidth(), getShowHeight()) / 2 / mScale * Math.max(scaleH,scaleW) / Math.min(scaleH,scaleW), Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_ROUND://纠结是该让gif圆角化还是view圆角化，貌似让gif圆角好看点。但是按理是该让view圆角。当填充方式是centerscrop时两者效果一样
				mPath.addRoundRect(rect, new float[]{boder_radius/mScale, boder_radius/mScale, boder_radius/mScale, boder_radius/mScale,boder_radius/mScale, boder_radius/mScale, boder_radius/mScale, boder_radius/mScale}, Path.Direction.CCW);
				canvas.clipPath(mPath, Region.Op.REPLACE);
				break;
			case TYPE_RECTANGLE:
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
