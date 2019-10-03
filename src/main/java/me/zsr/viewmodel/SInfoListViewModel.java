package me.zsr.viewmodel;

import java.util.ArrayList;
import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rssmodel.ModelAction;
import me.zsr.rssmodel.ModelObserver;
import me.zsr.rssmodel.SInfoModel;

public class SInfoListViewModel {
    private ViewModelObserver<Article> mObserver;
    private List<Article> mLiveDataList = new ArrayList<>();
    private ModelObserver<Article> mModelObserver = new ModelObserver<Article>() {
        @Override
        public void onDataChanged(ModelAction action, List<Article> dataList) {
            switch (action) {
                case ADD:
                    for (Article article : dataList) {
                        mLiveDataList.add(article.clone());
                    }
                    mObserver.onDataChanged(mLiveDataList);
                    break;
            }
        }
    };

    public SInfoListViewModel(ViewModelObserver<Article> observer) {
        mObserver = observer;
        SInfoModel.getInstance().registerObserver(mModelObserver);
    }

    public void load() {
        SInfoModel.getInstance().fetch();
    }
}
