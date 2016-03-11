package com.github.nukc.buff;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by C on 19/11/2015.
 */
public class TextRefreshView extends FrameLayout implements IPullUIHandler{

    private int mHeight;

    private TextView mTextView;

    public TextRefreshView(Context context) {
        this(context, null);
    }

    public TextRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            mHeight = Utils.dpToPx(64);
        }

        mTextView = new TextView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, mHeight);
        layoutParams.gravity = Gravity.BOTTOM;
        mTextView.setLayoutParams(layoutParams);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextColor(getResources().getColor(android.R.color.white));
        addView(mTextView);

        setBackgroundResource(R.color.gray_dark);
    }

    @Override
    public void onPulling(float scrollTop, int targetY, int totalDragDistance) {
        if (scrollTop < mHeight) {
            mTextView.setText(getContext().getString(R.string.pull_down_to_refresh));
        } else {
            mTextView.setText(getContext().getString(R.string.release_to_refresh));
        }
    }

    @Override
    public void onRefresh(int totalDragDistance) {
        mTextView.setText(getContext().getString(R.string.loading));
    }

    @Override
    public void onStop(float dragPercent) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, mHeight);
    }
}
