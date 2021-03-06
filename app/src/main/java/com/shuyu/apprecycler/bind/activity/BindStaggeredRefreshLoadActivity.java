package com.shuyu.apprecycler.bind.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.shuyu.apprecycler.R;
import com.shuyu.apprecycler.bind.view.BindCustomLoadMoreFooter;
import com.shuyu.apprecycler.bind.view.BindCustomRefreshHeader;

import com.shuyu.apprecycler.bind.holder.BindClickHolder;
import com.shuyu.apprecycler.bind.holder.BindImageHolder;
import com.shuyu.apprecycler.bind.holder.BindMutliHolder;
import com.shuyu.apprecycler.bind.holder.BindNoDataHolder;
import com.shuyu.apprecycler.bind.holder.BindTextHolder;
import com.shuyu.apprecycler.bind.model.BindClickModel;
import com.shuyu.apprecycler.bind.model.BindImageModel;
import com.shuyu.apprecycler.bind.model.BindMutliModel;
import com.shuyu.apprecycler.bind.model.BindTextModel;
import com.shuyu.apprecycler.bind.utils.BindDataUtils;
import com.shuyu.bind.decoration.BindDecorationBuilder;
import com.shuyu.bind.BindSuperAdapter;
import com.shuyu.bind.BindSuperAdapterManager;
import com.shuyu.bind.listener.OnLoadingListener;
import com.shuyu.bind.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 带上下拉的瀑布流
 * Created by guoshuyu on 2017/1/7.
 */
public class BindStaggeredRefreshLoadActivity extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView recycler;

    private List datas = new ArrayList<>();

    private BindSuperAdapter adapter;

    private BindSuperAdapterManager normalAdapterManager;

    private final Object lock = new Object();

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_recycler_layout);
        ButterKnife.bind(this);
        init();
        refresh();
    }


    public void init() {
        View header = LayoutInflater.from(this).inflate(R.layout.layout_header, null);

        normalAdapterManager = new BindSuperAdapterManager();
        normalAdapterManager
                .bind(BindImageModel.class, BindImageHolder.ID, BindImageHolder.class)
                .bind(BindTextModel.class, BindTextHolder.ID, BindTextHolder.class)
                .bind(BindMutliModel.class, BindMutliHolder.ID, BindMutliHolder.class)
                .bind(BindClickModel.class, BindClickHolder.ID, BindClickHolder.class)
                .bindEmpty(BindNoDataHolder.NoDataModel.class, BindNoDataHolder.ID, BindNoDataHolder.class)
                .setNeedAnimation(true)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(Context context, int position) {
                        //需要减去你的header和刷新的view的数量
                        Toast.makeText(context, "点击了！！　" + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .addHeaderView(header)
                .setPullRefreshEnabled(true)
                .setLoadingMoreEnabled(true)
                .setFootView(new BindCustomLoadMoreFooter(this))
                .setRefreshHeader(new BindCustomRefreshHeader(this))
                .setLoadingListener(new OnLoadingListener() {
                    @Override
                    public void onRefresh() {
                        recycler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        }, 3000);
                    }

                    @Override
                    public void onLoadMore() {
                        recycler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadMore();
                            }
                        }, 2000);
                    }
                });
        ;

        adapter = new BindSuperAdapter(this, normalAdapterManager, datas);


        //瀑布流管理器
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        //staggeredGridLayoutManager.setReverseLayout(true);
        recycler.setLayoutManager(staggeredGridLayoutManager);

        //间隔线
        recycler.addItemDecoration(new BindDecorationBuilder(adapter)
                .setColor(getResources()
                .getColor(R.color.material_deep_teal_500))
                .setSpace(dip2px(this, 5))
                .setNeedGridRightLeftEdge(true)
                .setNeedFirstTopEdge(true)
                .builder());

        recycler.setAdapter(adapter);


    }

    /**
     * dip转为PX
     */
    public static int dip2px(Context context, float dipValue) {
        float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * fontScale + 0.5f);
    }

    private void refresh() {
        List list = BindDataUtils.getRefreshData();
        //组装好数据之后，再一次性给list，在加多个锁，这样能够避免和上拉数据更新冲突
        //数据要尽量组装好，避免多个异步操作同个内存，因为多个异步更新一个数据源会有问题。
        synchronized (lock) {
            datas = list;
            adapter.setListData(datas);
            normalAdapterManager.refreshComplete();
        }

    }

    private void loadMore() {
        List list = BindDataUtils.getLoadMoreData(datas);
        //组装好数据之后，再一次性给list，在加多个锁，这样能够避免和上拉数据更新冲突
        //数据要尽量组装好，避免多个异步操作同个内存，因为多个异步更新一个数据源会有问题。
        synchronized (lock) {
            adapter.addListData(list);
            if (count < 1) {
                normalAdapterManager.loadMoreComplete();
            } else {
                normalAdapterManager.setNoMore(true);
            }
            count++;
        }
    }


}
