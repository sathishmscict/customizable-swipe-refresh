package com.github.nukc.sample.rain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.github.nukc.sample.R;

/**
 * Created by C on 2016/1/27.
 */
public class WaveView extends View {

    private int mMaxHeight;
    private int mTargetY = 0;
    private int mWaveHeight = 0;
    private int mWaveMinHeight = 0;

    private Path mPath;
    private Paint mPaint;

    private Bitmap mBgBitmap;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(Color.argb(150, 43, 43, 43));
        mPaint.setAntiAlias(true);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        mMaxHeight = getResources().getDimensionPixelSize(R.dimen.view_footer_height);

        mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg, options);
        mBgBitmap = Bitmap.createScaledBitmap(mBgBitmap, mMaxHeight * 2, mMaxHeight, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int saveCount = canvas.save();

//        canvas.translate(0, -mTargetY);
        canvas.clipRect(0, getMeasuredHeight() - mTargetY, getMeasuredWidth(), getMeasuredHeight());

        drawBg(canvas);
        drawWave(canvas);

        canvas.restoreToCount(saveCount);
    }

    private void drawBg(Canvas canvas){
        canvas.drawBitmap(mBgBitmap, 0, 0, null);
    }

    private void drawWave(Canvas canvas){
        mPath.reset();
        mPath.moveTo(0, mMaxHeight);
        mPath.lineTo(0, mWaveMinHeight);
        mPath.quadTo(getMeasuredWidth() / 2, mMaxHeight - mWaveHeight, getMeasuredWidth(), mWaveMinHeight);
        mPath.lineTo(getMeasuredWidth(), mMaxHeight);

        canvas.drawPath(mPath, mPaint);
    }

    public int getTargetY() {
        return mTargetY;
    }

    public void setTargetY(int targetY) {
        this.mTargetY = targetY;
        invalidate();
    }

    public void setWaveHeight(int waveHeight) {
        this.mWaveHeight = waveHeight;
        invalidate();
    }

    public void setChange(int targetY, int totalDragDistance){
        this.mTargetY = targetY;
        this.mWaveHeight = targetY;
        this.mWaveMinHeight = mMaxHeight - totalDragDistance;
        invalidate();
    }
}
