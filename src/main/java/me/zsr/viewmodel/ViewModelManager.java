package me.zsr.viewmodel;

import android.content.Context;

import me.zsr.rssmodel.DBManager;

public class ViewModelManager {

    public static void init(Context context) {
        DBManager.init(context);
    }
}
