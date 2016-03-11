package com.github.nukc.sample.rain;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.nukc.buff.IPullUIHandler;
import com.github.nukc.buff.Utils;
import com.github.nukc.sample.R;

/**
 * Created by C on 2016/1/27.
 */
public class RainRefreshView extends FrameLayout implements IPullUIHandler {
    private int mHeight;

    private RainView mRainView;
    private WaveView mWaveView;
    private TextView tvTip;

    private int mCurrentTargetY;

    public RainRefreshView(Context context) {
        this(context, null);
    }

    public RainRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int rainViewHeight = 0;
        if (!isInEditMode()) {
            mHeight = getResources().getDimensionPixelSize(R.dimen.view_footer_height);
            rainViewHeight = getResources().getDimensionPixelSize(com.github.nukc.buff.R.dimen.inside_target);
        }

        LayoutParams _layoutParams = generateDefaultLayoutParams();
        _layoutParams.height = mHeight;
        setLayoutParams(_layoutParams);

        mWaveView = new WaveView(context);
        addView(mWaveView);

        mRainView = new RainView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, rainViewHeight);
        layoutParams.gravity = Gravity.BOTTOM;
        mRainView.setLayoutParams(layoutParams);
        addView(mRainView);

        tvTip = new TextView(context);
        layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, Utils.dpToPx(10));
        layoutParams.gravity = Gravity.BOTTOM;
        tvTip.setLayoutParams(layoutParams);
        tvTip.setGravity(Gravity.CENTER);
        tvTip.setTextColor(getResources().getColor(android.R.color.white));
        addView(tvTip);

        mRainView.setVisibility(GONE);
    }


    @Override
    public void onPulling(float scrollTop, int targetY, int totalDragDistance) {
        mCurrentTargetY = targetY;
        mWaveView.setChange(targetY, totalDragDistance);

        if (Math.abs(scrollTop) > totalDragDistance)
            tvTip.setText(getContext().getString(com.github.nukc.buff.R.string.release_to_load_more));
        else
            tvTip.setText(getContext().getString(com.github.nukc.buff.R.string.pull_up_to_load_more));
    }

    @Override
    public void onRefresh(int totalDragDistance) {
        ValueAnimator animatorY = ValueAnimator.ofInt(mCurrentTargetY, totalDragDistance);
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveView.setTargetY((int)animation.getAnimatedValue());
            }
        });
        animatorY.setDuration(200);
        animatorY.setInterpolator(new DecelerateInterpolator(2f));
        animatorY.start();

        tvTip.setText(getContext().getString(R.string.loading));
        mRainView.setVisibility(VISIBLE);
        mRainView.StartRain();

        ValueAnimator animator = ValueAnimator.ofInt(totalDragDistance >> 2, totalDragDistance);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveView.setWaveHeight((int) animation.getAnimatedValue());
            }
        });
        animator.setInterpolator(new BounceInterpolator());
        animator.setDuration(800);
        animator.start();

    }

    @Override
    public void onStop(float dragPercent) {
        mRainView.stopRain();
        mRainView.setVisibility(GONE);
        tvTip.setText(null);

        ValueAnimator animator = ValueAnimator.ofInt(mWaveView.getTargetY(), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveView.setTargetY((int) animation.getAnimatedValue());
            }
        });
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.setDuration(200);
        animator.setStartDelay(200);
        animator.start();
    }

}
