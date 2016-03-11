package com.github.nukc.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.nukc.buff.PageLayout;
import com.github.nukc.sample.rain.RainRefreshView;
import com.github.nukc.sample.sun.SunImageView;

import java.util.ArrayList;
import java.util.List;

public class InsideActivity extends AppCompatActivity {

    private static final int TIME_DELAY = 2000;

    private PageLayout mPageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside);

        setupPageLayout();

        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 13; i++){
            strings.add("Item: " + i);
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strings));
    }


    private void setupPageLayout(){
        mPageLayout = (PageLayout) findViewById(R.id.pageLayout);

        mPageLayout.setHeaderView(new SunImageView(this, mPageLayout));
        mPageLayout.setFooterView(new RainRefreshView(this));

        mPageLayout.setOnRefreshAndLoadMoreListener(new PageLayout.OnRefreshAndLoadMoreListener() {
            @Override
            public void onRefresh() {
                mPageLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPageLayout.setRefreshing(false);
                    }
                }, TIME_DELAY);
            }

            @Override
            public void onLoadMore() {
                mPageLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPageLayout.setLoadingMore(false);
                    }
                }, TIME_DELAY);
            }
        });
    }


}
