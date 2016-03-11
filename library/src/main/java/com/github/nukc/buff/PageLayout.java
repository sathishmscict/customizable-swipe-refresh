package com.github.nukc.buff;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

/**
 * Created by C on 21/12/2015.
 * Nukc
 */
public class PageLayout extends ViewGroup {

    private static final String TAG = PageLayout.class.getSimpleName();

    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final int ANIMATE_REFRESH_STOP_DURATION = 200;

    private static final int MODE_INSIDE = 0;
    private static final int MODE_OUTSIDE = 1;

    private View mHeaderView;
    private View mTarget;
    private IPullUIHandler mHeaderPullHandler;
    private LoadRetryLayout mLoadRetryLayout;
    private View mFooterView;
    private IPullUIHandler mFooterPullHandler;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private int mTouchSlop;
    private int mActivePointerId;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private int mTotalDragDistance;

    private float mCurrentDragPercent;

    private boolean mRefreshing = false;
    private boolean mFirstInitOnLayout = false;
    private boolean mLoadRetryEnabled;
    private boolean mLoadMoreEnabled;
    private boolean mIsLoadingMore = false;

    private static final byte PULL_UP = 0;
    private static final byte PULL_DOWN = 1;

    private byte mPullDirection;
    private int mLayoutMode;

    private OnRefreshListener mOnRefreshListener;
    private OnRefreshAndLoadMoreListener mOnRefreshAndLoadMoreListener;

    public PageLayout(Context context) {
        this(context, null);
    }

    public PageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageLayout);
        mLoadRetryEnabled = a.getBoolean(R.styleable.PageLayout_loadRetryEnabled, false);
        mLoadMoreEnabled = a.getBoolean(R.styleable.PageLayout_loadMoreEnabled, false);
        mLayoutMode = a.getInteger(R.styleable.PageLayout_layoutMode, MODE_OUTSIDE);
        a.recycle();

        if (mLayoutMode == MODE_INSIDE) {
            mTotalDragDistance = getResources().getDimensionPixelSize(R.dimen.inside_target);
        }else {
            mTotalDragDistance = getResources().getDimensionPixelSize(R.dimen.outside_target);
        }
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount > 2) {
            throw new IllegalStateException("PageLayout only can host 2 elements");
        } else if (childCount == 2) {
            // not specify header or content
            if (mTarget == null || mHeaderView == null) {
                View child1 = getChildAt(0);
                View child2 = getChildAt(1);
                if (child1 instanceof IPullUIHandler) {
                    mHeaderView = child1;
                    mTarget = child2;
                    mHeaderPullHandler = (IPullUIHandler) mHeaderView;
                } else if (child2 instanceof IPullUIHandler) {
                    mHeaderView = child2;
                    mTarget = child1;
                    mHeaderPullHandler = (IPullUIHandler) mHeaderView;
                } else {
                    // both are not specified
                    if (mTarget == null && mHeaderView == null) {
                        mHeaderView = child1;
                        mTarget = child2;
                    }
                    // only one is specified
                    else {
                        if (mHeaderView == null) {
                            mHeaderView = mTarget == child1 ? child2 : child1;
                        } else {
                            mTarget = mHeaderView == child1 ? child2 : child1;
                        }
                    }
                }
            }

        } else if (childCount == 1){
            mTarget = getChildAt(0);

            mHeaderView = new TextRefreshView(getContext());
            addView(mHeaderView, 0);
            mHeaderPullHandler = (IPullUIHandler) mHeaderView;
        }

        setFooterRefreshView();

        if (mLoadRetryEnabled) {
            mLoadRetryLayout = new LoadRetryLayout(getContext());
            addView(mLoadRetryLayout, getChildCount());
        }

        super.onFinishInflate();
    }

    private void setFooterRefreshView(){
        if (mLoadMoreEnabled && mFooterView == null) {
            mFooterView = new FooterRefreshView(getContext());
            mFooterPullHandler = (IPullUIHandler) mFooterView;
            addView(mFooterView, 0);
        }
    }

    public void setHeaderView(View headerView){
        if (mHeaderView != null){
            removeView(mHeaderView);
        }

        mHeaderView = headerView;
        addView(mHeaderView, 0);
        if (headerView instanceof IPullUIHandler){
            mHeaderPullHandler = (IPullUIHandler) mHeaderView;
        }
    }

    public void setFooterView(View footerView){
        if (mFooterView != null){
            removeView(mFooterView);
        }

        mFooterView = footerView;
        addView(mFooterView, 0);
        if (footerView instanceof IPullUIHandler){
            mFooterPullHandler = (IPullUIHandler) mFooterView;
        }
    }

    public void setTotalDragDistance(int totalDragDistance) {
        this.mTotalDragDistance = totalDragDistance;
    }

    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    public DecelerateInterpolator getDecelerateInterpolator() {
        return mDecelerateInterpolator;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null){
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mTarget != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mTarget.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin,
                    layoutParams.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + layoutParams.topMargin,
                    layoutParams.height);
            mTarget.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        if (mLoadRetryLayout != null){
            measureChildWithMargins(mLoadRetryLayout, widthMeasureSpec, 0 ,heightMeasureSpec, 0);
        }

        if (mFooterView != null){
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0 ,heightMeasureSpec, 0);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();
    }

    private void layoutChildren() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);

            //第一次初始化的时候设置头视图的位置，避免下拉刷新过程中发生了onLayout事件又设置了头视图的位置，先这样吧- -
            if (!mFirstInitOnLayout && mLayoutMode == MODE_OUTSIDE) {
                ViewCompat.setTranslationY(mHeaderView, -mHeaderView.getMeasuredHeight());
                mFirstInitOnLayout = !mFirstInitOnLayout;
                Log.i(TAG, "first init");
            }
        }

        if (mTarget != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mTarget.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mTarget.getMeasuredWidth();
            final int bottom = top + mTarget.getMeasuredHeight();
            mTarget.layout(left, top, right, bottom);
        }

        if (mLoadRetryLayout != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mLoadRetryLayout.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mLoadRetryLayout.getMeasuredWidth();
            final int bottom = top + mLoadRetryLayout.getMeasuredHeight();
            mLoadRetryLayout.layout(left, top, right, bottom);
        }

        if (mFooterView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + mFooterView.getMeasuredHeight();
            if (mLayoutMode == MODE_OUTSIDE) {
                mFooterView.layout(left, getBottom(), right, getBottom() + bottom);
            }else {
                mFooterView.layout(left, getBottom() - bottom, right, getBottom());
            }
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mRefreshing || mIsLoadingMore){
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action){
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialMotionY = initialDownY;
            case MotionEvent.ACTION_MOVE:

                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged && !canChildScrollUp()) {
                    mIsBeingDragged = true;
                    mPullDirection = PULL_DOWN;
                }else if (yDiff < 0 && !canChildScrollDown() && mLoadMoreEnabled){
                    mIsBeingDragged = true;
                    mPullDirection = PULL_UP;
                }

                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
//            case MotionEvent.ACTION_DOWN:
//                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
//                mIsBeingDragged = false;
//                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;
                final float scrollTop = yDiff * DRAG_RATE;

                mCurrentDragPercent = scrollTop / mTotalDragDistance;

                float dragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                float extraOS = Math.abs(scrollTop) - mTotalDragDistance;
                float slingshotDist = mTotalDragDistance;
                float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2)
                        / slingshotDist);
                float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                        (tensionSlingshotPercent / 4), 2)) * 2f;
                float extraMove = (slingshotDist) * tensionPercent * 2;

                int targetY = (int) ((slingshotDist * dragPercent) + extraMove);

                if (scrollTop > 0 && mPullDirection == PULL_DOWN) {
                    mHeaderPullHandler.onPulling(scrollTop, targetY, mTotalDragDistance);
                    ViewCompat.setTranslationY(mTarget, targetY);
                    if (mLayoutMode == MODE_OUTSIDE)
                        ViewCompat.setTranslationY(mHeaderView, targetY - mHeaderView.getHeight());

                } else if (scrollTop < 0 && mLoadMoreEnabled && mPullDirection == PULL_UP){

                    if (mLayoutMode == MODE_OUTSIDE) {
                        ViewCompat.setTranslationY(mFooterView, -targetY);
                        mFooterPullHandler.onPulling(scrollTop, targetY, mTotalDragDistance);
                        ViewCompat.setTranslationY(mTarget, -targetY);
                    }else if (targetY <= mFooterView.getHeight()){
                        mFooterPullHandler.onPulling(scrollTop, targetY, mTotalDragDistance);
                        ViewCompat.setTranslationY(mTarget, -targetY);
                    }
                } else {
                    return false;
                }

                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                if (overScrollTop > mTotalDragDistance && mPullDirection == PULL_DOWN){
                    setRefreshing(true);
                    pullDownToCorrectPosition(); //< -mFooterView.getHeight()
                } else if (mLoadMoreEnabled && overScrollTop < -mTotalDragDistance && mPullDirection == PULL_UP){
                    setLoadingMore(true);
                    pullUpToCorrectPosition();
                } else {
                    mIsLoadingMore = false;
                    mRefreshing = false;
                    animateToStartPosition();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }

        }

        return true;
    }

    private void animateToStartPosition(){
        if (mPullDirection == PULL_DOWN) {
            mHeaderPullHandler.onStop(mCurrentDragPercent);
        }else if (mLoadMoreEnabled){
            mFooterPullHandler.onStop(mCurrentDragPercent);
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLayoutMode == MODE_OUTSIDE) {
                    if (mPullDirection == PULL_DOWN) {
                        ViewCompat.animate(mHeaderView)
                                .translationY(-mHeaderView.getHeight())
                                .setInterpolator(mDecelerateInterpolator)
                                .setDuration(ANIMATE_TO_START_DURATION)
                                .start();
                    } else if (mLoadMoreEnabled) {
                        ViewCompat.animate(mFooterView)
                                .translationY(0)
                                .setDuration(ANIMATE_TO_START_DURATION)
                                .setInterpolator(mDecelerateInterpolator)
                                .start();
                    }
                }

                ViewCompat.animate(mTarget)
                        .translationY(0)
                        .setDuration(ANIMATE_TO_START_DURATION)
                        .setInterpolator(mDecelerateInterpolator)
                        .start();
            }
        }, ANIMATE_REFRESH_STOP_DURATION);

    }

    private void pullDownToCorrectPosition(){
        if (mLayoutMode == MODE_OUTSIDE)
            ViewCompat.animate(mHeaderView)
                    .translationY(mTotalDragDistance - mHeaderView.getHeight())
                    .setDuration(ANIMATE_TO_TRIGGER_DURATION)
                    .setInterpolator(mDecelerateInterpolator)
                    .start();

        ViewCompat.animate(mTarget)
                .translationY(mTotalDragDistance)
                .setDuration(ANIMATE_TO_TRIGGER_DURATION)
                .setInterpolator(mDecelerateInterpolator)
                .start();
    }

    private void pullUpToCorrectPosition(){
        if (mLayoutMode == MODE_OUTSIDE) {
            ViewCompat.animate(mFooterView)
                    .translationY(-mTotalDragDistance) //-mFooterView.getHeight()
                    .setDuration(ANIMATE_TO_TRIGGER_DURATION)
                    .setInterpolator(mDecelerateInterpolator)
                    .start();
        }

        ViewCompat.animate(mTarget)
                .translationY(-mTotalDragDistance)//-mFooterView.getHeight()
                .setDuration(ANIMATE_TO_TRIGGER_DURATION)
                .setInterpolator(mDecelerateInterpolator)
                .start();
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }


    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }


    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            mRefreshing = refreshing;
            if (mRefreshing) {
                callListener();
                mHeaderPullHandler.onRefresh(mTotalDragDistance);
            } else {
                animateToStartPosition();
            }
        }
    }

    public void setLoadingMore(boolean loadingMore){
        if (mIsLoadingMore != loadingMore) {
            mIsLoadingMore = loadingMore;
            if (mIsLoadingMore) {
                callListener();
                mFooterPullHandler.onRefresh(mTotalDragDistance);
            } else {
                animateToStartPosition();
            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public void setLoadMoreEnabled(boolean loadMoreEnabled) {
        this.mLoadMoreEnabled = loadMoreEnabled;
        setFooterRefreshView();
    }

    public LoadRetryLayout getLoadRetryLayout() {
        if (mLoadRetryLayout == null)
            throw new NullPointerException("LoadRetryLayout is null, The LoadRetryEnabled is false? You can set the mLoadRetryEnabled = true.");
        return mLoadRetryLayout;
    }

    public void showLoading(boolean show){
        mLoadRetryLayout.showLoadingView(show);
        showContent(!show);
    }

    public void showRetry(boolean show){
        mLoadRetryLayout.showRetryView(show);
        showContent(!show);
    }

    public void showEmpty(boolean show){
        mLoadRetryLayout.showEmptyView(show);
        showContent(!show);
    }

    private void showContent(boolean show){
        int visibility = show ? VISIBLE : GONE;
        mHeaderView.setVisibility(visibility);
        mTarget.setVisibility(visibility);
    }

    /**
     * Called when requesting data successfully
     */
    public void onRequestSuccess(){
        showContent(true);
        mLoadRetryLayout.setVisibility(GONE);
    }

    /**
     * Called when requesting data failed
     */
    public void onRequestFailure(){
        showRetry(true);
    }

    /**
     * Set the retry button click listener
     * @param listener
     */
    public void setOnRetryClickListener(LoadRetryLayout.OnRetryClickListener listener){
        if (mLoadRetryLayout != null){
            mLoadRetryLayout.setOnRetryClickListener(listener);
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    public void setOnRefreshAndLoadMoreListener(OnRefreshAndLoadMoreListener listener){
        mOnRefreshAndLoadMoreListener = listener;
    }

    private void callListener(){
        if (mOnRefreshListener != null){
            mOnRefreshListener.onRefresh();
        }else if (mOnRefreshAndLoadMoreListener != null){
            if (mPullDirection == PULL_UP){
                mOnRefreshAndLoadMoreListener.onLoadMore();
            }else {
                mOnRefreshAndLoadMoreListener.onRefresh();
            }
        }
    }


    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnRefreshAndLoadMoreListener {
        void onRefresh();

        void onLoadMore();
    }

}