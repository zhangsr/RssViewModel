package me.zsr.viewmodel;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.zsr.rssbean.Discover;
import me.zsr.rsscommon.FileUtil;
import me.zsr.rsscommon.LogUtil;
import me.zsr.rsscommon.ThreadManager;

public class PresetDiscoverViewModel extends DiscoverViewModel {
    private List<Discover> mLiveDataList;
    private List<Discover> mCacheDataList;

    public PresetDiscoverViewModel(ViewModelObserver<Discover> observer, Context context) {
        super(observer, context);
    }

    public void preloadData() {
        ThreadManager.postInBackground(new Runnable() {
            @Override
            public void run() {
                String presetJson = FileUtil.readAssetFie(mContext, "preset_discover.json");
                try {
                    JSONObject jsonObject = new JSONObject(presetJson);
                    String listStr = jsonObject.getString("list");
                    Type listType = new TypeToken<List<Discover>>() {}.getType();
                    mCacheDataList = new Gson().fromJson(listStr, listType);

                    mLiveDataList = new ArrayList<>();
                    for (Discover discover : mCacheDataList) {
                        mLiveDataList.add(discover.clone());
                    }

                    ThreadManager.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mObserver != null) {
                                mObserver.onDataChanged(mLiveDataList);
                            }
                        }
                    });

                    LogUtil.i("preloadData end, size=" + mCacheDataList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
