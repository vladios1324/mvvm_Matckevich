package com.example.mvvm_matckevich.presentations;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mvvm_matckevich.domains.models.Day;

import java.util.List;

public class BindingAdapters {
    @BindingAdapter("app:days")
    public static void setDays(RecyclerView recyclerView, List<Day> days) {
        if(recyclerView.getAdapter() instanceof DayAdapter) {
            DayAdapter adapter = (DayAdapter) recyclerView.getAdapter();
            adapter.setDays(days);
        }
    }
}
