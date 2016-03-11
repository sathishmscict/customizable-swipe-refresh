package com.github.nukc.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.nukc.buff.LoadRetryLayout;
import com.github.nukc.buff.PageLayout;

public class OutsideActivity extends AppCompatActivity {

    private static final int TIME_DELAY = 2000;
    private PageLayout mPageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside);

        int colors[] = {
                android.R.color.background_light,
                R.color.yellow_a200, R.color.yellow_500,
                R.color.amber_a200, R.color.amber_500,
                R.color.orange_a200, R.color.orange_500,
                R.color.orange_deep_a200, R.color.orange_deep_500
        };

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        SampleAdapter sampleAdapter = new SampleAdapter(this, colors);
        recyclerView.setAdapter(sampleAdapter);

        setupPageLayout();

        mPageLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                //调用后显示点击重试界面（请求失败后）
                mPageLayout.onRequestFailure();
            }
        }, TIME_DELAY);

    }

    private void setupPageLayout(){
        mPageLayout = (PageLayout) findViewById(R.id.pageLayout);

        //设置自定义正在加载
        mPageLayout.getLoadRetryLayout().setLoadingView(R.layout.view_loading);

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

        mPageLayout.setOnRetryClickListener(new LoadRetryLayout.OnRetryClickListener() {
            @Override
            public void onRetryClick(View v) {
                mPageLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //调用后显示内容（请求成功后）
                        mPageLayout.onRequestSuccess();
                    }
                }, TIME_DELAY);
            }
        });
    }

    static class SampleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private final LayoutInflater mInflater;
        private int colors[];

        public SampleAdapter(Context context, int colors[]) {
            mInflater = LayoutInflater.from(context);
            this.colors = colors;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemHolder _holder = (ItemHolder) holder;
            _holder.bg.setBackgroundColor(_holder.itemView.getResources().getColor(colors[position]));
            _holder.tv.setText(position + "");
        }

        @Override
        public int getItemCount() {
            return colors.length;
        }

        static class ItemHolder extends RecyclerView.ViewHolder{
            View bg;
            TextView tv;

            public ItemHolder(View itemView) {
                super(itemView);
                bg = itemView.findViewById(R.id.bg);
                tv = (TextView) itemView.findViewById(R.id.tv);
            }
        }
    }
}
