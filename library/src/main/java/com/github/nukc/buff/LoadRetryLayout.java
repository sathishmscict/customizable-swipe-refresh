package com.github.nukc.buff;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Created by C on 24/12/2015.
 * Nukc
 */
public class LoadRetryLayout extends FrameLayout {

    private LayoutInflater mInflater;
    private View mLoadingView;
    private View mRetryView;
    private View mEmptyView;

    private OnRetryClickListener mListener;

    public LoadRetryLayout(Context context) {
        this(context, null);
    }

    public LoadRetryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        setBackgroundResource(android.R.color.background_light);

        setLoadingView(R.layout.view_loading);
        if (isInEditMode()) mLoadingView.setVisibility(GONE);

        setRetryView(R.layout.view_retry);
        mRetryView.setVisibility(GONE);

        setEmptyView(R.layout.view_empty);
        mEmptyView.setVisibility(GONE);
    }

    public void setLoadingView(@LayoutRes int resource){
        View view = mInflater.inflate(resource, this, false);
        setLoadingView(view);
    }

    public void setLoadingView(View view){
        if (mLoadingView != null){
            removeView(mLoadingView);
        }
        mLoadingView = view;
        addView(mLoadingView);
    }

    public void setRetryView(@LayoutRes int resource){
        View view = mInflater.inflate(resource, this, false);
        setRetryView(view);
    }

    public void setRetryView(View view){
        if (mRetryView != null){
            removeView(mRetryView);
        }
        mRetryView = view;
        addView(mRetryView);
        setRetryBtnClick();
    }

    public void setEmptyView(@LayoutRes int resource){
        View view = mInflater.inflate(resource, this, false);
        setEmptyView(view);
    }

    public void setEmptyView(View view){
        if (mEmptyView != null){
            removeView(mEmptyView);
        }
        mEmptyView = view;
        addView(mEmptyView);
    }

    public void showLoadingView(boolean show){
        mLoadingView.setVisibility(show ? VISIBLE : GONE);
        mRetryView.setVisibility(GONE);
        mEmptyView.setVisibility(GONE);
        showIf();
    }

    public void showRetryView(boolean show){
        mRetryView.setVisibility(show ? VISIBLE : GONE);
        mLoadingView.setVisibility(GONE);
        mEmptyView.setVisibility(GONE);
        showIf();
    }

    public void showEmptyView(boolean show){
        mEmptyView.setVisibility(show ? VISIBLE : GONE);
        mLoadingView.setVisibility(GONE);
        mRetryView.setVisibility(GONE);
        showIf();
    }

    public void showIf(){
        int visibilityCount = 0;
        for (int i = 0; i < getChildCount(); i++){
            if (getChildAt(i).getVisibility() == VISIBLE)
                visibilityCount++;
        }

        if (visibilityCount > 0){
            setVisibility(VISIBLE);
        }else {
            setVisibility(GONE);
        }
    }

    private void setRetryBtnClick(){
        if (mRetryView instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) mRetryView;
            for (int i = 0; i < parent.getChildCount(); i++){
                View childView = parent.getChildAt(i);
                if (childView instanceof Button){
                    childView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLoadingView(true);
                            if (mListener != null){
                                mListener.onRetryClick(v);
                            }
                        }
                    });
                }
            }
        }
    }

    public void setOnRetryClickListener(OnRetryClickListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRetryClickListener{
        void onRetryClick(View v);
    }
}
