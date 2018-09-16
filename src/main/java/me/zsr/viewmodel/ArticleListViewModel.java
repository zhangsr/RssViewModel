package me.zsr.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rssbean.Subscription;
import me.zsr.rsscommon.ThreadManager;
import me.zsr.rssmodel.ArticleModel;
import me.zsr.rssmodel.ModelAction;
import me.zsr.rssmodel.ModelObserver;

public class ArticleListViewModel {
    private ViewModelObserver<Article> mObserver;
    private List<Article> mLiveDataList;
    private List<Article> mCacheDataList = new ArrayList<>();
    private boolean mIsLoading;
    private ModelObserver<Article> mModelObserver = new ModelObserver<Article>() {
        @Override
        public void onDataChanged(ModelAction action, List<Article> dataList) {
            switch (action) {
                case MODIFY:
                    if (mLiveDataList == null) {
                        break;
                    }

                    for (Article modifiedArticle : dataList) {
                        for (Article liveArticle : mLiveDataList) {
                            if (modifiedArticle.getId().equals(liveArticle.getId())) {
                                update(liveArticle, modifiedArticle);
                            }
                        }
                    }
                    break;
            }

            mObserver.onDataChanged(mLiveDataList);
        }
    };

    private void update(Article oldA, Article newA) {
        // TODO: 2018/5/19 update more
        oldA.setRead(newA.getRead());
    }

    public ArticleListViewModel(ViewModelObserver<Article> observer) {
        mObserver = observer;
        ArticleModel.getInstance().registerObserver(mModelObserver);
    }

    public void loadFav() {
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;

        ThreadManager.postInBackground(new Runnable() {
            @Override
            public void run() {
                mCacheDataList.clear();
                mCacheDataList.addAll(ArticleModel.getInstance().queryFav());

                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        mLiveDataList = new ArrayList<>();
                        for (Article article : mCacheDataList) {
                            mLiveDataList.add(article.clone());
                        }
                        mObserver.onDataChanged(mLiveDataList);
                        mIsLoading = false;
                    }
                });
            }
        });
    }

    public void load(final Subscription... subscriptions) {
        List<Long> ids = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            ids.add(subscription.getId());
        }
        load(ids);
    }

    public void load(Long... ids) {
        load(Arrays.asList(ids));
    }

    public void load (final List<Long> subscriptionIds) {
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;

        ThreadManager.postInBackground(new Runnable() {
            @Override
            public void run() {
                mCacheDataList.clear();
                for (long id : subscriptionIds) {
                    mCacheDataList.addAll(ArticleModel.getInstance().queryBySubscriptionIdSync(id));
                }

                ThreadManager.post(new Runnable() {
                    @Override
                    public void run() {
                        mLiveDataList = new ArrayList<>();
                        for (Article article : mCacheDataList) {
                            mLiveDataList.add(article.clone());
                        }
                        mObserver.onDataChanged(mLiveDataList);
                        mIsLoading = false;
                    }
                });
            }
        });
    }

    public void onItemClick(List<Article> dataList, int pos) {
        if (dataList == null || pos >= dataList.size()) {
            return;
        }

        Article data = dataList.get(pos);
        if (!data.getRead()) {
            ArticleModel.getInstance().markRead(true, data);
        }

    }

}
