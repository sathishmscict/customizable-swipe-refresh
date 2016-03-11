package com.github.nukc.sample.sun;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.github.nukc.buff.IPullUIHandler;
import com.github.nukc.buff.PageLayout;

/**
 * Created by C on 29/1/2016.
 * Nukc
 */
public class SunImageView extends ImageView implements IPullUIHandler {

    private PageLayout mPageLayout;
    private BaseRefreshView mBaseRefreshView;

    private int mTotalDragDistance;
    private float mCurrentPercent;

    public SunImageView(Context context, PageLayout pageLayout) {
        this(context, null, pageLayout);
    }

    public SunImageView(Context context, AttributeSet attrs, PageLayout pageLayout) {
        super(context, attrs);
        mBaseRefreshView = new SunRefreshView(context, pageLayout);
        setImageDrawable(mBaseRefreshView);
        mPageLayout = pageLayout;
        mTotalDragDistance = pageLayout.getTotalDragDistance();
    }

    @Override
    public void onPulling(float scrollTop, int targetY, int totalDragDistance) {
        float percent = scrollTop / totalDragDistance;
        if (percent < 0) {
            return;
        }

        mCurrentPercent = percent;
        mBaseRefreshView.setPercent(percent, true);
        mBaseRefreshView.offsetTopAndBottom(targetY);
    }

    @Override
    public void onRefresh(int totalDragDistance) {
        ValueAnimator animator = ValueAnimator.ofFloat(mCurrentPercent, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBaseRefreshView.setPercent((float) animation.getAnimatedValue(), true);
                mBaseRefreshView.offsetTopAndBottom((int) (mTotalDragDistance *  (float)animation.getAnimatedValue()));
            }
        });
        animator.setDuration(200);
        animator.setInterpolator(mPageLayout.getDecelerateInterpolator());
        animator.start();

        mBaseRefreshView.start();
    }

    @Override
    public void onStop(float dragPercent) {
        ValueAnimator animator = ValueAnimator.ofInt((int)(mTotalDragDistance * Math.min(1, Math.abs(dragPercent))), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBaseRefreshView.setPercent((float)(((int)(animation.getAnimatedValue())) / mTotalDragDistance - 1), false);
                mBaseRefreshView.offsetTopAndBottom((int) animation.getAnimatedValue());

            }
        });
        animator.setInterpolator(mPageLayout.getDecelerateInterpolator());
        animator.setDuration(200);
        animator.setStartDelay(200);
        animator.start();
    }

}
