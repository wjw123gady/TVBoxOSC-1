package com.github.tvbox.osc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.player.controller.VodController;
import com.github.tvbox.osc.ui.adapter.GridAdapter;
import com.github.tvbox.osc.ui.adapter.SeriesAdapter;
import com.github.tvbox.osc.ui.adapter.SeriesFlagAdapter;
import com.github.tvbox.osc.ui.dialog.QuickSearchDialog;
import com.github.tvbox.osc.ui.fragment.PlayerFragment;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MD5;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.ListLayoutManager;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.jessyan.autosize.utils.AutoSizeUtils;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class DetailActivity extends BaseActivity {
    private LinearLayout llLayout;
    private ImageView ivThumb;
    private TextView tvName;
    private TextView tvYear;
    private TextView tvSite;
    private TextView tvArea;
    private TextView tvLang;
    private TextView tvType;
    private TextView tvActor;
    private TextView tvDirector;
    private TextView tvDes;
    private LinearLayout tvPlay;
    private LinearLayout tv3rdPlay;
    private TextView tv3rdPlayName;
    private LinearLayout tvSort;
    private LinearLayout tvQuickSearch;
    private LinearLayout tvCollect;
    private TvRecyclerView mGridViewFlag;
    private TvRecyclerView mGridView;
    private TvRecyclerView mSeriesGroupView;
    private LinearLayout mEmptyPlayList;
    private LinearLayout seriesGroupLayout;
    private SourceViewModel sourceViewModel;
    private Movie.Video mVideo;
    private VodInfo vodInfo;
    private SeriesFlagAdapter seriesFlagAdapter;
    private SeriesAdapter seriesAdapter;
    private BaseQuickAdapter<String, BaseViewHolder> seriesGroupAdapter;
    public String vodId;
    public String sourceKey;
    boolean seriesSelect = false;
    private View seriesFlagFocus = null;
    private int lastSeriesFocusIndex = -1;
    private FrameLayout mPlayerFrame;
    private static PlayerFragment playerFragment;
    private ArrayList<String> seriesGroupOptions = new ArrayList<>();

    private static final int DETAIL_PLAYER_FRAME_ID = 9999999;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_detail;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        initView();
        initViewModel();
        initData();
    }

    private void initView() {
        llLayout = findViewById(R.id.llLayout);
        ivThumb = findViewById(R.id.ivThumb);
        tvName = findViewById(R.id.tvName);
        tvYear = findViewById(R.id.tvYear);
        tvSite = findViewById(R.id.tvSite);
        tvArea = findViewById(R.id.tvArea);
        tvLang = findViewById(R.id.tvLang);
        tvType = findViewById(R.id.tvType);
        tvActor = findViewById(R.id.tvActor);
        tvDirector = findViewById(R.id.tvDirector);
        tvDes = findViewById(R.id.tvDes);
        tvPlay = findViewById(R.id.tvPlay);
        tvSort = findViewById(R.id.tvSort);
        tvCollect = findViewById(R.id.tvCollect);
        tvQuickSearch = findViewById(R.id.tvQuickSearch);
        mEmptyPlayList = findViewById(R.id.mEmptyPlaylist);
        seriesGroupLayout = findViewById(R.id.seriesGroupLayout);
        tv3rdPlay = findViewById(R.id.tv3rdPlay);
        tv3rdPlayName = findViewById(R.id.tv3rdPlayName);
        playerFragment = new PlayerFragment();
        mGridView = findViewById(R.id.mGridView);
        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, LinearLayoutManager.HORIZONTAL, false));
        seriesAdapter = new SeriesAdapter();
        mGridView.setAdapter(seriesAdapter);
        mGridViewFlag = findViewById(R.id.mGridViewFlag);
        mGridViewFlag.setHasFixedSize(true);
        mGridViewFlag.setLayoutManager(new V7LinearLayoutManager(this.mContext, LinearLayoutManager.HORIZONTAL, false));
        seriesFlagAdapter = new SeriesFlagAdapter();
        mGridViewFlag.setAdapter(seriesFlagAdapter);
        mPlayerFrame = findViewById(R.id.mPlayerFrame);
        mSeriesGroupView = findViewById(R.id.mSeriesGroupView);
        mSeriesGroupView.setHasFixedSize(true);
        mSeriesGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, LinearLayoutManager.HORIZONTAL, false));
        seriesGroupAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_series, seriesGroupOptions) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                TextView tvSeries = helper.getView(R.id.tvSeries);
                tvSeries.setText(item);
            }
        };
        mSeriesGroupView.setAdapter(seriesGroupAdapter);
        tvSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vodInfo != null && vodInfo.seriesMap.size() > 0) {
                    vodInfo.reverseSort = !vodInfo.reverseSort;
                    vodInfo.reverse();
                    vodInfo.playIndex = vodInfo.seriesMap.get(vodInfo.playFlag).size() - 1 - vodInfo.playIndex;
                    insertVod(sourceKey, vodInfo);
                    seriesAdapter.notifyDataSetChanged();
                    Collections.reverse(seriesGroupOptions);
                    seriesGroupAdapter.notifyDataSetChanged();
                }
            }
        });
        tvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                jumpToPlay(true, false);
            }
        });
        tvQuickSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuickSearch();
                QuickSearchDialog quickSearchDialog = new QuickSearchDialog(DetailActivity.this);
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH, quickSearchData));
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_WORD, quickSearchWord));
                quickSearchDialog.show();
                if (pauseRunnable != null && pauseRunnable.size() > 0) {
                    searchExecutorService = Executors.newFixedThreadPool(5);
                    for (Runnable runnable : pauseRunnable) {
                        searchExecutorService.execute(runnable);
                    }
                    pauseRunnable.clear();
                    pauseRunnable = null;
                }
                quickSearchDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        try {
                            if (searchExecutorService != null) {
                                pauseRunnable = searchExecutorService.shutdownNow();
                                searchExecutorService = null;
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            }
        });
        tvCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomDataManger.insertVodCollect(sourceKey, vodInfo);
                Toast.makeText(DetailActivity.this, "已加入收藏夹", Toast.LENGTH_SHORT).show();
            }
        });
        mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                seriesSelect = false;
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                seriesSelect = true;
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        mGridViewFlag.setOnItemListener(new TvRecyclerView.OnItemListener() {
            private void refresh(View itemView, int position) {
                String newFlag = seriesFlagAdapter.getData().get(position).name;
                if (vodInfo != null && !vodInfo.playFlag.equals(newFlag)) {
                    for (int i = 0; i < vodInfo.seriesFlags.size(); i++) {
                        VodInfo.VodSeriesFlag flag = vodInfo.seriesFlags.get(i);
                        if (flag.name.equals(vodInfo.playFlag)) {
                            flag.selected = false;
                            seriesFlagAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    VodInfo.VodSeriesFlag flag = vodInfo.seriesFlags.get(position);
                    flag.selected = true;
                    vodInfo.playFlag = newFlag;
                    seriesFlagAdapter.notifyItemChanged(position);
                    refreshList();
                }
                seriesFlagFocus = itemView;
            }

            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                refresh(itemView, position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                refresh(itemView, position);
            }
        });
        mGridViewFlag.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            @Override
            public boolean onInBorderKeyEvent(int direction, View focused) {
                if(direction == View.FOCUS_DOWN)
                    mGridView.requestFocus();
                return false;
            }
        });
        seriesAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (vodInfo != null && vodInfo.seriesMap.get(vodInfo.playFlag).size() > 0) {
                    if (vodInfo.playIndex != position) {
                        seriesAdapter.getData().get(vodInfo.playIndex).selected = false;
                        seriesAdapter.notifyItemChanged(vodInfo.playIndex);
                        seriesAdapter.getData().get(position).selected = true;
                        seriesAdapter.notifyItemChanged(position);
                        vodInfo.playIndex = position;
                        lastSeriesFocusIndex = position;
                    }
                    if(playerFragment != null) {
                        VodInfo playingInfo = playerFragment.getPlayingVodInfo();
                        if(playingInfo != null && playingInfo.playFlag.equals(vodInfo.playFlag) && playingInfo.playIndex == vodInfo.playIndex ) {
                            jumpToPlay(true, false);
                        } else {
                            jumpToPlay(true, true);
                        }
                    } else {
                        jumpToPlay(true, true);
                    }
                }
            }
        });
        mSeriesGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                TextView txtView = itemView.findViewById(R.id.tvSeries);
                txtView.setTextColor(Color.WHITE);
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                TextView txtView = itemView.findViewById(R.id.tvSeries);
                txtView.setTextColor(mContext.getResources().getColor(R.color.color_02F8E1));
                if (vodInfo != null && vodInfo.seriesMap.get(vodInfo.playFlag).size() > 0) {
                    int targetPos = position * 20;
                    if (vodInfo.playIndex != targetPos) {
                        mGridView.scrollToPosition(targetPos);
                    }
                }
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        mPlayerFrame.post(new Runnable() {
            @Override
            public void run() {
                playerFragment.getVodController().enableController(false);
            }
        });
        tv3rdPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer[] types = PlayerHelper.getAvailable3rdPlayerTypes();
                if(types.length > 0) {
                    Integer selectedType = Hawk.get(HawkConfig.THIRD_PARTY_PLAYER, types[0]);
                    playerFragment.playInOtherPlayer(selectedType);
                }
            }
        });
        init3rdPlayerButton();
        setLoadSir(llLayout);
    }

    private void init3rdPlayerButton() {
        PlayerHelper.reload3rdPlayers();
        Integer[] types = PlayerHelper.getAvailable3rdPlayerTypes();
        if(types.length <= 0)
            tv3rdPlay.setVisibility(View.GONE);
        else {
            tv3rdPlay.setVisibility(View.VISIBLE);
            Integer selectedType = Hawk.get(HawkConfig.THIRD_PARTY_PLAYER, types[0]);
            if(Arrays.binarySearch(types, selectedType) < 0)
                Hawk.put(HawkConfig.THIRD_PARTY_PLAYER, types[0]);
            tv3rdPlayName.setText(PlayerHelper.get3rdPlayerName(selectedType));
        }
    }

    private void insertPlayerFragment() {
        FrameLayout tempFrame = new FrameLayout(this);
        tempFrame.setId(DETAIL_PLAYER_FRAME_ID);
        mPlayerFrame.addView(tempFrame, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        getSupportFragmentManager()
                .beginTransaction()
                .add(DETAIL_PLAYER_FRAME_ID, playerFragment, PlayerFragment.FRAGMENT_TAG)
                .disallowAddToBackStack()
                .commit();
    }

    private List<Runnable> pauseRunnable = null;

    private void jumpToPlay(boolean shouldOpenActivity, boolean newSource) {
        if (vodInfo != null && vodInfo.seriesMap.get(vodInfo.playFlag).size() > 0) {
            if(shouldOpenActivity) {
                getSupportFragmentManager().beginTransaction().remove(playerFragment).commit();
                if(newSource) {
                    //保存历史
                    insertVod(sourceKey, vodInfo);
                    playerFragment.destroy();
                    playerFragment = new PlayerFragment();
                } else {
                    playerFragment.getVodController().enableController(true);
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("newSource", newSource);
                bundle.putString("sourceKey", sourceKey);
                bundle.putSerializable("VodInfo", vodInfo);
                jumpActivity(PlayActivity.class, bundle);
            } else {
                if(newSource)
                    insertVod(sourceKey, vodInfo);
                if(playerFragment != null)
                    playerFragment.initData(vodInfo, sourceKey);
            }
        }
    }

    void refreshList() {
        if (vodInfo.seriesMap.get(vodInfo.playFlag).size() <= vodInfo.playIndex) {
            vodInfo.playIndex = 0;
        }

        if (vodInfo.seriesMap.get(vodInfo.playFlag) != null) {
            vodInfo.seriesMap.get(vodInfo.playFlag).get(vodInfo.playIndex).selected = true;
        }

        List<VodInfo.VodSeries> seriesList = vodInfo.seriesMap.get(vodInfo.playFlag);
        if(seriesList.size() > 0)
            seriesGroupLayout.setVisibility(View.VISIBLE);
        else
            seriesGroupLayout.setVisibility(View.GONE);
        seriesAdapter.setNewData(seriesList);
        seriesGroupOptions.clear();
        int optionSize = seriesList.size() / 20;
        int remainedOptionSize = seriesList.size() % 20;
        for(int i = 0; i < optionSize; i++) {
            if(vodInfo.reverseSort)
                seriesGroupOptions.add(String.format("%d - %d", i * 20 + 20, i * 20 + 1));
            else
                seriesGroupOptions.add(String.format("%d - %d", i * 20 + 1, i * 20 + 20));
        }
        if(remainedOptionSize > 0) {
            if(vodInfo.reverseSort)
                seriesGroupOptions.add(String.format("%d - %d", optionSize * 20 + remainedOptionSize, optionSize * 20));
            else
                seriesGroupOptions.add(String.format("%d - %d", optionSize * 20, optionSize * 20 + remainedOptionSize));
        }
        if(vodInfo.reverseSort)
            Collections.reverse(seriesGroupOptions);
        seriesGroupAdapter.notifyDataSetChanged();
        mGridView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGridView.scrollToPosition(vodInfo.playIndex);
            }
        }, 100);
    }

    private void setTextShow(TextView view, String tag, String info) {
        if (info == null || info.trim().isEmpty()) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setText(Html.fromHtml(getHtml(tag, info)));
    }

    private String removeHtmlTag(String info) {
        if (info == null)
            return "";
        return info.replaceAll("\\<.*?\\>", "").replaceAll("\\s", "");
    }

    private void initViewModel() {
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        sourceViewModel.detailResult.observe(this, new Observer<AbsXml>() {
            @Override
            public void onChanged(AbsXml absXml) {
                if (absXml != null && absXml.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size() > 0) {
                    showSuccess();
                    mVideo = absXml.movie.videoList.get(0);
                    vodInfo = new VodInfo();
                    vodInfo.setVideo(mVideo);
                    vodInfo.sourceKey = mVideo.sourceKey;

                    tvName.setText(mVideo.name);
                    setTextShow(tvSite, "来源：", ApiConfig.get().getSource(mVideo.sourceKey).getName());
                    setTextShow(tvYear, "年份：", mVideo.year == 0 ? "" : String.valueOf(mVideo.year));
                    setTextShow(tvArea, "地区：", mVideo.area);
                    setTextShow(tvLang, "语言：", mVideo.lang);
                    setTextShow(tvType, "类型：", mVideo.type);
                    setTextShow(tvActor, "演员：", mVideo.actor);
                    setTextShow(tvDirector, "导演：", mVideo.director);
                    setTextShow(tvDes, "内容简介：", removeHtmlTag(mVideo.des));
                    if (!TextUtils.isEmpty(mVideo.pic)) {
                        Picasso.get()
                                .load(DefaultConfig.checkReplaceProxy(mVideo.pic))
                                .transform(new RoundTransformation(MD5.string2MD5(mVideo.pic + mVideo.name))
                                        .centerCorp(true)
                                        .override(AutoSizeUtils.mm2px(mContext, 300), AutoSizeUtils.mm2px(mContext, 400))
                                        .roundRadius(AutoSizeUtils.mm2px(mContext, 10), RoundTransformation.RoundType.ALL))
                                .placeholder(R.drawable.img_loading_placeholder)
                                .error(R.drawable.img_loading_placeholder)
                                .into(ivThumb);
                    } else {
                        ivThumb.setImageResource(R.drawable.img_loading_placeholder);
                    }

                    if (vodInfo.seriesMap != null && vodInfo.seriesMap.size() > 0) {
                        mGridViewFlag.setVisibility(View.VISIBLE);
                        mGridView.setVisibility(View.VISIBLE);
                        tvPlay.setVisibility(View.VISIBLE);
                        mEmptyPlayList.setVisibility(View.GONE);

                        VodInfo vodInfoRecord = RoomDataManger.getVodInfo(sourceKey, vodId);
                        // 读取历史记录
                        if (vodInfoRecord != null) {
                            vodInfo.playIndex = Math.max(vodInfoRecord.playIndex, 0);
                            vodInfo.playFlag = vodInfoRecord.playFlag;
                            vodInfo.playerCfg = vodInfoRecord.playerCfg;
                            vodInfo.reverseSort = vodInfoRecord.reverseSort;
                        } else {
                            vodInfo.playIndex = 0;
                            vodInfo.playFlag = null;
                            vodInfo.playerCfg = "";
                            vodInfo.reverseSort = false;
                        }

                        if (vodInfo.reverseSort) {
                            vodInfo.reverse();
                        }

                        if (vodInfo.playFlag == null || !vodInfo.seriesMap.containsKey(vodInfo.playFlag))
                            vodInfo.playFlag = (String) vodInfo.seriesMap.keySet().toArray()[0];

                        int flagScrollTo = 0;
                        for (int j = 0; j < vodInfo.seriesFlags.size(); j++) {
                            VodInfo.VodSeriesFlag flag = vodInfo.seriesFlags.get(j);
                            if (flag.name.equals(vodInfo.playFlag)) {
                                flagScrollTo = j;
                                flag.selected = true;
                            } else
                                flag.selected = false;
                        }

                        seriesFlagAdapter.setNewData(vodInfo.seriesFlags);
                        mGridViewFlag.scrollToPosition(flagScrollTo);

                        refreshList();
                        jumpToPlay(false, true);
                        // startQuickSearch();
                    } else {
                        mGridViewFlag.setVisibility(View.GONE);
                        mGridView.setVisibility(View.GONE);
                        tvPlay.setVisibility(View.GONE);
                        mEmptyPlayList.setVisibility(View.VISIBLE);
                    }
                } else {
                    showEmpty();
                }
            }
        });
    }

    private String getHtml(String label, String content) {
        if (content == null) {
            content = "";
        }
        return label + "<font color=\"#FFFFFF\">" + content + "</font>";
    }

    public static PlayerFragment getManagedPlayerFragment() {
        return DetailActivity.playerFragment;
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            loadDetail(bundle.getString("id", null), bundle.getString("sourceKey", ""));
        }
    }

    private void loadDetail(String vid, String key) {
        if (vid != null) {
            vodId = vid;
            sourceKey = key;
            showLoading();
            sourceViewModel.getDetail(sourceKey, vodId);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_REFRESH) {
            if (event.obj != null) {
                if (event.obj instanceof Integer) {
                    int index = (int) event.obj;
                    if (index != lastSeriesFocusIndex) {
                        if(lastSeriesFocusIndex >= 0) {
                            seriesAdapter.getData().get(lastSeriesFocusIndex).selected = false;
                            seriesAdapter.notifyItemChanged(lastSeriesFocusIndex);
                        }
                        lastSeriesFocusIndex = index;
                        seriesAdapter.getData().get(index).selected = true;
                        seriesAdapter.notifyItemChanged(index);
                        mGridView.setSelection(index);
                        vodInfo.playIndex = index;
                        //保存历史
                        insertVod(sourceKey, vodInfo);
                    }
                } else if (event.obj instanceof JSONObject) {
                    vodInfo.playerCfg = ((JSONObject) event.obj).toString();
                    //保存历史
                    insertVod(sourceKey, vodInfo);
                }

            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_SELECT) {
            if (event.obj != null) {
                Movie.Video video = (Movie.Video) event.obj;
                loadDetail(video.id, video.sourceKey);
            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_WORD_CHANGE) {
            if (event.obj != null) {
                String word = (String) event.obj;
                switchSearchWord(word);
            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_RESULT) {
            try {
                searchData(event.obj == null ? null : (AbsXml) event.obj);
            } catch (Exception e) {
                searchData(null);
            }
        }
    }

    private String searchTitle = "";
    private boolean hadQuickStart = false;
    private List<Movie.Video> quickSearchData = new ArrayList<>();
    private List<String> quickSearchWord = new ArrayList<>();
    private ExecutorService searchExecutorService = null;

    private void switchSearchWord(String word) {
        OkGo.getInstance().cancelTag("quick_search");
        quickSearchData.clear();
        searchTitle = word;
        searchResult();
    }

    private void startQuickSearch() {
        if (hadQuickStart)
            return;
        hadQuickStart = true;
        OkGo.getInstance().cancelTag("quick_search");
        quickSearchWord.clear();
        searchTitle = mVideo.name;
        quickSearchData.clear();
        quickSearchWord.add(searchTitle);
        // 分词
        OkGo.<String>get("http://api.pullword.com/get.php?source=" + URLEncoder.encode(searchTitle) + "&param1=0&param2=0&json=1")
                .tag("fenci")
                .execute(new AbsCallback<String>() {
                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        if (response.body() != null) {
                            return response.body().string();
                        } else {
                            throw new IllegalStateException("网络请求错误");
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String json = response.body();
                        quickSearchWord.clear();
                        try {
                            for (JsonElement je : new Gson().fromJson(json, JsonArray.class)) {
                                quickSearchWord.add(je.getAsJsonObject().get("t").getAsString());
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                        quickSearchWord.add(searchTitle);
                        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_WORD, quickSearchWord));
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });

        searchResult();
    }

    private void searchResult() {
        try {
            if (searchExecutorService != null) {
                searchExecutorService.shutdownNow();
                searchExecutorService = null;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        searchExecutorService = Executors.newFixedThreadPool(5);
        List<SourceBean> searchRequestList = new ArrayList<>();
        searchRequestList.addAll(ApiConfig.get().getSourceBeanList());
        SourceBean home = ApiConfig.get().getHomeSourceBean();
        searchRequestList.remove(home);
        searchRequestList.add(0, home);

        ArrayList<String> siteKey = new ArrayList<>();
        for (SourceBean bean : searchRequestList) {
            if (!bean.isSearchable() || !bean.isQuickSearch()) {
                continue;
            }
            siteKey.add(bean.getKey());
        }
        for (String key : siteKey) {
            searchExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    sourceViewModel.getQuickSearch(key, searchTitle);
                }
            });
        }
    }

    private void searchData(AbsXml absXml) {
        if (absXml != null && absXml.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size() > 0) {
            List<Movie.Video> data = new ArrayList<>();
            for (Movie.Video video : absXml.movie.videoList) {
                // 去除当前相同的影片
                if (video.sourceKey.equals(sourceKey) && video.id.equals(vodId))
                    continue;
                data.add(video);
            }
            quickSearchData.addAll(data);
            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH, data));
        }
    }

    private void insertVod(String sourceKey, VodInfo vodInfo) {
        try {
            vodInfo.playNote = vodInfo.seriesMap.get(vodInfo.playFlag).get(vodInfo.playIndex).name;
        } catch (Throwable th) {
            vodInfo.playNote = "";
        }
        RoomDataManger.insertVodRecord(sourceKey, vodInfo);
        EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_HISTORY_REFRESH));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (searchExecutorService != null) {
                searchExecutorService.shutdownNow();
                searchExecutorService = null;
            }
            if(playerFragment != null)
                playerFragment.destroy();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        OkGo.getInstance().cancelTag("fenci");
        OkGo.getInstance().cancelTag("detail");
        OkGo.getInstance().cancelTag("quick_search");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init3rdPlayerButton();
        if(getSupportFragmentManager().findFragmentByTag(PlayerFragment.FRAGMENT_TAG) == null) {
            mPlayerFrame.removeAllViews();
            insertPlayerFragment();
        }
        VodController controller = playerFragment.getVodController();
        if(controller != null) {
            controller.enableController(false);
        }
        VideoView videoView = playerFragment.getVideoView();
        if (videoView != null) {
            videoView.resume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(playerFragment != null) {
            VideoView videoView = playerFragment.getVideoView();
            if (videoView != null) {
                videoView.pause();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (seriesSelect) {
            if (seriesFlagFocus != null && !seriesFlagFocus.isFocused()) {
                seriesFlagFocus.requestFocus();
                return;
            }
        }
        if(playerFragment != null) {
            playerFragment.destroy();
            playerFragment = null;
        }
        super.onBackPressed();
    }
}