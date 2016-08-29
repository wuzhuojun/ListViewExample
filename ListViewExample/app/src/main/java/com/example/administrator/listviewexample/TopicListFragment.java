package com.example.administrator.listviewexample;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.example.administrator.listviewexample.adapter.TopicListAdapter;
import com.example.administrator.listviewexample.model.TopicJson;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by Administrator on 2016/8/26.
 */
public class TopicListFragment extends Fragment implements AbsListView.OnScrollListener {

    private TopicListAdapter topicListAdapter;

    @BindView(R.id.list_topic)
    ListView mTopicListView;

    @BindView(R.id.rotate_header_list_view_frame)
    protected PtrClassicFrameLayout mPtrFrame;//下拉刷新的控件

    private ACache mCache;
    private final String topicKey = "topic_list_";

    private int currentPage = 1;
    private int lastVisibleItem = 0; //最后可以见item

    private int totalItemCount = 0; //总item数量
    private View footView; //底部加载更多界面布局
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_list, container, false);
        ButterKnife.bind(this, view);

        mCache = ACache.get(getActivity());

        initView();
        initData();
        return view;
    }

    private void initView()
    {
        topicListAdapter = new TopicListAdapter(getActivity());
        footView = getActivity().getLayoutInflater().inflate(R.layout.footer_item, null);
        footView.setVisibility(View.GONE);
        mTopicListView.addFooterView(footView);
        mTopicListView.setAdapter(topicListAdapter);
        mTopicListView.setOnScrollListener(this);


        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                updateData();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });

        // the following are default settings
        mPtrFrame.setResistance(1.7f);
        mPtrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
        mPtrFrame.setDurationToClose(200);
        mPtrFrame.setDurationToCloseHeader(1000);
        // default is false
        mPtrFrame.setPullToRefresh(false);
        // default is true
        mPtrFrame.setKeepHeaderWhenRefresh(true);

    }

    private void initData()
    {
        new GetHttpDataTask().execute();
    }

    protected void updateData() {
        mPtrFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                topicListAdapter.clear();
                currentPage = 1;
                new GetHttpDataTask().execute();
                mPtrFrame.refreshComplete();
            }
        }, 0);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (lastVisibleItem == totalItemCount && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            //当前可见的最后item位置 与 总item数量相等，表示已经滑到底部了
            footView.setVisibility(View.VISIBLE);
            currentPage++;
            new GetHttpDataTask().execute();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
    }


    private class GetHttpDataTask extends AsyncTask<String, Integer, String> {
        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            //优先从缓存中获取到数据
            String strContent = mCache.getAsString(topicKey + currentPage);
            if (null != strContent) {
                return strContent;
            }

            //只有当缓存没有数据时才从网络中获取数据
            try {
                String param = "sortby=new&boardId=2&pageSize=20&page=" + currentPage +"&accessToken=c29b8a10fd19ce6c0b8f15a1acd28&accessSecret=26ff9822a3f0b9fa33f53a8d0f8ce";
                strContent = executePost("http://beta.www.coolxap.com/openapi/app/web/index.php?r=forum/topiclist", param);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != strContent) {
                mCache.put(topicKey, strContent, 60);
            }

            return strContent;
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            if (null != result) {
                Gson gson = new Gson();
                TopicJson topicJson = gson.fromJson(result, TopicJson.class);

                topicListAdapter.addDatas(topicJson.getList());
            }
        }
    }


    OkHttpClient client = new OkHttpClient();
    /**
     * Post 网络请求
     */
    public static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    String executePost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
