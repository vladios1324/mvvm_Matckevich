package com.example.mvvm_matckevich.viewmodels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mvvm_matckevich.datas.apis.WeatherApi;
import com.example.mvvm_matckevich.datas.apis.WeatherResponse;
import com.example.mvvm_matckevich.datas.callbacks.MyResponseCallback;
import com.example.mvvm_matckevich.datas.databases.WeatherContext;
import com.example.mvvm_matckevich.domains.models.Day;
import com.example.mvvm_matckevich.presentations.utils.DataNotifier;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayViewModel  extends ViewModel {
    MutableLiveData<List<Day>>  _days = new MutableLiveData<>();
    public LiveData<List<Day>> days = _days;
    MutableLiveData<String> _nowTemp = new MutableLiveData<>();
    public LiveData<String> nowTemp = _nowTemp;
    MutableLiveData<String> _condition = new MutableLiveData<>();
    public LiveData<String> condition = _condition;

    public void updateLocation(double lat, double lon) {
        WeatherApi weatherApi = new WeatherApi(lat, lon, ResponseWeather);
        weatherApi.execute();
    }

    public DayViewModel() {
        loadDays();
        DataNotifier.getInstance().subscribe(this::loadDays);
    }

    public void loadDays() {
        new Thread(() -> {
            List<Day> days = WeatherContext.allDays();

            new Handler(Looper.getMainLooper()).post(() -> {
               _days.setValue(days);
               if(days.isEmpty() == false) {
                   _nowTemp.setValue(days.get(0).Temp + "°");
                   _condition.setValue(days.get(0).Condition);
               }
            });
        }).start();
    }

    MyResponseCallback ResponseWeather = new MyResponseCallback() {
        @Override
        public void onCompile(String result) {
            List<Day> dayList = new ArrayList<>();
            WeatherResponse weatherResponse = new GsonBuilder().create().fromJson(
                    result,
                    WeatherResponse.class
            );

            for(WeatherResponse.Forecast forecast : weatherResponse.forecasts) {
                if(forecast.hours.isEmpty()) continue;
                Integer avgTemp = avgTemp(forecast.hours);
                String nameDay = getDayOfWeek(forecast.date);
                String condition = getDayCondition(forecast.hours);
                Day day = new Day(nameDay, avgTemp, condition);
                dayList.add(day);
            }
            _days.setValue(dayList);
            _nowTemp.setValue(weatherResponse.fact.temp + "°");
            _condition.setValue(weatherResponse.fact.condition);
        }

        @Override
        public void onError(String error) {}
    };

    public Integer avgTemp(List<WeatherResponse.Forecast.Hour> hours) {
        Float sumTemp = 0f;
        for(WeatherResponse.Forecast.Hour hour : hours)
            sumTemp += hour.temp;
        Float avgTemp = sumTemp / hours.size();
        return Math.round(avgTemp);
    }

    public String getDayOfWeek(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("en"));
    }

    public String getDayCondition(List<WeatherResponse.Forecast.Hour> hours) {
        Map<String, Integer> conditionCount = new HashMap<>();
        for(WeatherResponse.Forecast.Hour hour : hours) {
            if(hour.condition != null && !hour.condition.isEmpty()) {
                conditionCount.put(hour.condition, conditionCount.getOrDefault(hour.condition, 0) + 1);
            }
        }

        String mostFrequentConditon = null;
        int maxCount = 0;
        for(Map.Entry<String, Integer> entry : conditionCount.entrySet()) {
            if(entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentConditon = entry.getKey();
            }
        }
        return mostFrequentConditon;
    }
}
