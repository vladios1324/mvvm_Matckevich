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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.mvvm_matckevich.R;
import com.example.mvvm_matckevich.databinding.ActivityMainBinding;
import com.example.mvvm_matckevich.datas.databases.DbContext;
import com.example.mvvm_matckevich.datas.databases.WeatherContext;
import com.example.mvvm_matckevich.datas.workers.WeatherWorker;
import com.example.mvvm_matckevich.viewmodels.DayViewModel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    DayViewModel viewModel;

    DayAdapter adapter;
    LocationManager locationManager;
    DbContext _context;
    public static double lastLat = 58.0;
    public static double lastLon = 56.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _context = new DbContext(this);
        onStartWorker();

        if(WeatherContext.allDays().isEmpty()) {
            onStartWorkerNow();
       }


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(DayViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        setupRecyclerView();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationPermission();
    }

    public void onStartWorker() {a
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                WeatherWorker.class,
                15, TimeUnit.MINUTES,
                30, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WORKER_MANAGER",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
    public void onStartWorkerNow() {
        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(WeatherWorker.class)
                .build();
        WorkManager.getInstance(this).enqueue(immediateWork);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 10, locationListener);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastLocation != null) {
                viewModel.updateLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            lastLat = location.getLatitude();
            lastLon = location.getLongitude();
            if (location != null) {
                viewModel.updateLocation(lastLat, lastLon);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Разрешение на геолокацию не получено", Toast.LENGTH_LONG).show();
        }
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