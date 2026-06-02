package com.example.mvvm_matckevich.datas.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mvvm_matckevich.datas.apis.WeatherApi;
import com.example.mvvm_matckevich.datas.apis.WeatherResponse;
import com.example.mvvm_matckevich.datas.callbacks.MyResponseCallback;
import com.example.mvvm_matckevich.datas.databases.WeatherContext;
import com.example.mvvm_matckevich.domains.models.Day;
import com.example.mvvm_matckevich.presentations.MainActivity;
import com.example.mvvm_matckevich.presentations.utils.DataNotifier;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WeatherWorker extends Worker {
    String TAG = "WeatherWorker";

    CountDownLatch _latch = new CountDownLatch(1);

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Выполнение запроса получения погоды");
        try {
            double lat = MainActivity.lastLat;
            double lon = MainActivity.lastLon;

            Log.d(TAG, "Координаты: " + lat + ", " + lon);

            WeatherApi weatherApi = new WeatherApi( lat, lon, ResponseWeather);
            weatherApi.execute();
            boolean completed = _latch.await( 30, TimeUnit.SECONDS);
            if(!completed) {
                Log.e(TAG, "Request failed");
                return Result.failure();
            }
            else {
                DataNotifier.getInstance().notifyUpdate();
                return Result.success();
            }
        } catch (InterruptedException e) {
            return Result.failure();
        }
    }

    MyResponseCallback ResponseWeather = new MyResponseCallback() {
        @Override
        public void onCompile(String result) {
            List<Day> daysList = new ArrayList<>();
            WeatherResponse weatherResponse = new GsonBuilder().create().fromJson(
                    result,
                    WeatherResponse.class
            );
            for (WeatherResponse.Forecast forecast : weatherResponse.forecasts) {
                if(forecast.hours.isEmpty()) continue;
                Integer avgTemp = avgTemp(forecast.hours);
                String nameDay = getDayOfWeek(forecast.date);
                String condition = getDayCondition(forecast.hours);
                Day day = new Day(nameDay, avgTemp, condition);
                daysList.add(day);
            }
            WeatherContext.Save(daysList);
            _latch.countDown();
        }

        @Override
        public void onError(String error) {}
    };

    public Integer avgTemp(List<WeatherResponse.Forecast.Hour> hours) {
        Float sumTemp = 0f;
        for (WeatherResponse.Forecast.Hour hour : hours) {
            sumTemp += hour.temp;
        }
        Float avgTemp = sumTemp / hours.size();
        return Math.round(avgTemp);
    }

    public String getDayOfWeek(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale( "en"));
    }

    public String getDayCondition(List<WeatherResponse.Forecast.Hour> hours) {
        Map<String, Integer> conditionCount = new HashMap<>();
        for (WeatherResponse.Forecast.Hour hour : hours) {
            if (hour.condition != null && !hour.condition.isEmpty()) {
                conditionCount.put(hour.condition,
                        conditionCount.getOrDefault(hour.condition, 0) + 1);
            }
        }
        String mostFrequentCondition = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : conditionCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentCondition = entry.getKey();
            }
        }
        return mostFrequentCondition;
    }
}
