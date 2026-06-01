package com.example.mvvm_matckevich.presentations;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.mvvm_matckevich.R;
import com.example.mvvm_matckevich.databinding.ActivityMainBinding;
import com.example.mvvm_matckevich.viewmodels.DayViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    DayViewModel viewModel;

    DayAdapter adapter;

    LocationManager locationManager;
    private boolean isLocationUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(DayViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        setupRecyclerView();


    }

    private void setupRecyclerView() {
        if (adapter == null) {
            adapter = new DayAdapter();
            binding.recyclerView.setAdapter(adapter);
        } else {
            adapter = (DayAdapter) binding.recyclerView.getAdapter();
        }
        viewModel.days.observe(this, days -> {
            if (days != null) {
                adapter.setDays(days);
            } else {
                adapter.setDays(new ArrayList<>());
            }
        });
    }
}