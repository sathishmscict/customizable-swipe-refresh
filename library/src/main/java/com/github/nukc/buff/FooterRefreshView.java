package com.github.nukc.buff;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by C on 5/1/2016.
 * Nukc
 */
public class FooterRefreshView extends FrameLayout implements IPullUIHandler {

    private TextView mTextView;
    private int mHeight;

    private String sReleaseToLoadMore;
    private String sPullUpToLoadMore;

    public FooterRefreshView(Context context) {
        this(context, null);
    }

    public FooterRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_refresh_footer, this);
        mTextView = (TextView) findViewById(R.id.text);

        mHeight = Utils.dpToPx(64);
        sReleaseToLoadMore = context.getString(R.string.release_to_load_more);
        sPullUpToLoadMore = context.getString(R.string.pull_up_to_load_more);
    }

    @Override
    public void onPulling(float scrollTop, int targetY, int totalDragDistance) {
        if (-scrollTop < mHeight){
            mTextView.setText(sPullUpToLoadMore);
        }else {
            mTextView.setText(sReleaseToLoadMore);
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
