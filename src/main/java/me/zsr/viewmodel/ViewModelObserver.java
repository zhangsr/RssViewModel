package me.zsr.viewmodel;

import java.util.List;

public interface ViewModelObserver<T> {
    void onDataChanged(List<T> dataList);
    void onRequestUpdate();
}
