package download.imageLoader.config;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class FailedDrawable extends Drawable {
    private Paint mPaint;
    private int txsize;
    private int txcolor;

    public FailedDrawable(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.txcolor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect r = getBounds();
        float cx = r.exactCenterX();
        float cy = r.exactCenterY();
        txsize = (int) Math.min((cx/2/2),cy * 0.8);
        mPaint.setTextSize(txsize);
        mPaint.setColor(txcolor);
        canvas.drawText("加载失败", cx - mPaint.measureText("加载失败") / 2, cy + txsize / 2, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        // not sure, so be safe
        return PixelFormat.TRANSLUCENT;
    }

}
