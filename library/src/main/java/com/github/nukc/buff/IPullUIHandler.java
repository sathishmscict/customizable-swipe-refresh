package com.github.nukc.buff;

/**
 * Created by C on 20/1/2016.
 * Nukc
 */
public interface IPullUIHandler {
    void onPulling(float scrollTop, int targetY, int totalDragDistance);

    void onRefresh(int totalDragDistance);

    void onStop(float dragPercent);
}
