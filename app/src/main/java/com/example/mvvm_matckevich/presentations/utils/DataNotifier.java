package com.example.mvvm_matckevich.presentations.utils;

import java.util.ArrayList;
import java.util.List;

public class DataNotifier {
    static DataNotifier _instance;
    List<Runnable> _listeners = new ArrayList<>();
    public static DataNotifier getInstance() {
        if(_instance == null) {
            _instance = new DataNotifier();
        }
        return _instance;
    }

    public void subscribe(Runnable listener) {
        _listeners.add(listener);
    }

    public void notifyUpdate() {
        for(Runnable listener : _listeners) {
            listener.run();
        }
    }
}
