package cn.denua.v2ex.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.denua.v2ex.R;
import cn.denua.v2ex.adapter.PullRefreshReplyAdapter;
import cn.denua.v2ex.base.BaseNetworkActivity;
import cn.denua.v2ex.interfaces.ResponseListener;
import cn.denua.v2ex.model.Reply;
import cn.denua.v2ex.model.Topic;
import cn.denua.v2ex.service.TopicService;
import cn.denua.v2ex.utils.HtmlUtil;
import cn.denua.v2ex.utils.StringUtil;
import cn.denua.v2ex.widget.TopicView;


public class TopicActivity extends BaseNetworkActivity{

    private WebView mWebView;
//    private TextView mTvContent;
    private TopicView mTopicView;
    private LinearLayout mLlHeader;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.rv_reply)
    RecyclerView mRecyclerView;

    private int mTopicId = -1;
    private Topic mTopic = null;
    private PullRefreshReplyAdapter mPullRecyclerAdapter;
    private String mErrorMsg = null;

    private int mPageCount;
    private int mCurrentPage = 1;
    private List<Reply> mReplies = new ArrayList<>();

    private ResponseListener<List<Reply>> mRepliesListener = new ResponseListener<List<Reply>>() {
        @Override
        public void onComplete(List<Reply> result) {

            mReplies.addAll(result);
            if (mPageCount == mCurrentPage) {
                mPullRecyclerAdapter.setStatus(PullRefreshReplyAdapter.FooterStatus.COMPLETE);
            }else {
                mPullRecyclerAdapter.setStatus(PullRefreshReplyAdapter.FooterStatus.LOADING);
            }
            mPullRecyclerAdapter.notifyRangeChanged(
                    mReplies.size() - result.size(), result.size());
        }
        @Override
        public void onFailed(String msg) {
            ToastUtils.showShort(msg);
        }
    };

    private ResponseListener<Topic> mTopicListener = new ResponseListener<Topic>() {
        @Override
        public void onComplete(Topic result) {
            mPageCount = result.getReplies() / 100 + 1;
            if (mTopicId == -1 && mTopic != null && mTopic.getContent_rendered() == null){
                mWebView.loadData(HtmlUtil.applyHtmlStyle(result.getContent_rendered()),
                        "text/html", "utf-8");
                mTopicView.setLastTouched(StringUtil.timestampToStr(result.getCreated()));
            }
            loadReplies(result);
        }
        private void loadReplies(Topic result){
            mTopic = result;
            mReplies.clear();
            mReplies.addAll(mTopic.getReplyList());
            if (mPageCount == mCurrentPage) {
                mPullRecyclerAdapter.setStatus(PullRefreshReplyAdapter.FooterStatus.COMPLETE);
            }
            mPullRecyclerAdapter.notifyDataSetChanged();
        }
        @Override
        public void onFailed(String msg) {

            mSwipeRefreshLayout.setRefreshing(false);
            ToastUtils.showShort(msg);
            if ((null != mErrorMsg) && !mErrorMsg.equals(msg)){
                TextView mTvError = new TextView(TopicActivity.this);
                mTvError.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 600));
                mTvError.setTextSize(25);
                mTvError.setTextColor(getColorAccent());
                mTvError.setGravity(Gravity.CENTER);
                mTvError.setText(msg);

                mLlHeader.removeAllViews();
                mLlHeader.addView(mTvError);
                mErrorMsg = msg;
            }
        }
    };

    private static void start(Context context, int topicId, Topic topic){

        Intent intent = new Intent(context, TopicActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("topicId", topicId);
        if (topic != null) intent.putExtra("topic", topic);
        context.startActivity(intent);
    }

    public static void start(Context context, int topicId){
        start(context, topicId, null);
    }

    public static void start(Context context, Topic topic){
        start(context, -1, topic);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_topic);
        ButterKnife.bind(this);

        setTitle(R.string.topic);
        mTopic = getIntent().getParcelableExtra("topic");
        mTopicId = getIntent().getIntExtra("topicId",-1);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReplies.size() == 0){
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.stopLoading();
        mWebView.destroy();
        mWebView = null;
    }

    /**
     * 初始化 RecyclerView
     */
    protected void initView(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mPullRecyclerAdapter = new PullRefreshReplyAdapter(this, mReplies);
        mPullRecyclerAdapter.setOnPullUpListener(this::loadNextPage);
        mRecyclerView.setAdapter(mPullRecyclerAdapter);

        setSwipeRefreshTheme(mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        initHeaderView();
    }

    /**
     * 初始化话题相关信息的 view
     */
    private void initHeaderView(){

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLlHeader = new LinearLayout(this);
        mLlHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        mLlHeader.setOrientation(LinearLayout.VERTICAL);
        mLlHeader.setLayoutParams(linearLayoutParams);
        
        mTopicView = new TopicView(this, false);
        mTopicView.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));

//        mTvContent = new TextView(this);
        mWebView = new WebView(this);
        mWebView.setNetworkAvailable(true);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setNetworkAvailable(true);
        mWebView.setFocusable(false);

        if (mTopic != null){
            setTitle(mTopic.getTitle());
            mPageCount = mTopic.getReplies() / 100 + 1;
            mTopicView.setTopic(mTopic);
            if (mTopic.getContent_rendered() != null){
//                mTvContent.setText(Html.fromHtml(HtmlUtil.applyHtmlStyle(mTopic.getContent_rendered())));
                mWebView.loadData(HtmlUtil.applyHtmlStyle(mTopic.getContent_rendered()),
                        "text/html", "utf-8");
            }
        }

        mLlHeader.addView(mTopicView);
        mLlHeader.addView(mWebView);
//        mLlHeader.addView(mTvContent);
        mPullRecyclerAdapter.setHeaderView(mLlHeader);
        mPullRecyclerAdapter.notifyItem(0);
    }

    private void onRefresh(){

        mCurrentPage = 1;
        mPullRecyclerAdapter.setStatus(PullRefreshReplyAdapter.FooterStatus.LOADING);
        int topicId =(mTopicId == -1 ? mTopic.getId() : mTopicId);
        TopicService.getTopicAndReply(this, topicId, 1, mTopicListener);

    }

    private void loadNextPage(){

        TopicService.getReply(this, mTopic.getId(), ++mCurrentPage, mRepliesListener);
    }

    @Override
    public void onStartRequest() {
        super.onStartRequest();
    }

    @Override
    public void onCompleteRequest() {
        super.onCompleteRequest();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public int getContextStatus() {
        return super.getContextStatus();
    }


}
