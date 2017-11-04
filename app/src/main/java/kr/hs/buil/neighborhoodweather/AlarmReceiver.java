package kr.hs.buil.neighborhoodweather;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import kr.hs.buil.neighborhoodweather.model.weather.Hourly;
import kr.hs.buil.neighborhoodweather.model.weather.Weather;
import kr.hs.buil.neighborhoodweather.model.weather.WeatherInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by user on 2017-11-04.
 */

public class AlarmReceiver extends BroadcastReceiver {
    Context mContext;
    Intent mIntent;
    
    
    WeatherService APIService;
    LocationManager locationManager = null;
    LocationListener locationListener = null;
    
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    
    double latitude = 0.0;
    double longitude = 0.0;
    
    String currentCity;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
        
        APIService = WeatherService.retrofit.create(WeatherService.class);
        
    }
    boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},0);
            return false;
        }
        return true;
    }
    void initLocationManager() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    void getLocationInfo() {
        if (isGPSEnabled || isNetworkEnabled) {
            final List<String> mListProviders = locationManager.getProviders(false);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    Address address = getAddressFromLocation(latitude, longitude);

                    if (address == null)
                        return;
                    String city;
                    String[] temp = address.getAddressLine(0).split(" ");

                    if (temp.length >= 3) {
                        if (temp[0].equals("대한민국"))
                            city = temp[1] + " " + temp[2] + " " + temp[3];
                        else
                            city = temp[0] + " " + temp[1] + " " + temp[2];
                    } else {
                        city = address.getAddressLine(0);
                    }
                    

                    locationManager.removeUpdates(locationListener);
                    currentCity = city;
                    
                    getCurrentWeather(latitude, longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            for (String name : mListProviders) {
                if (checkLocationPermission())
                    locationManager.requestLocationUpdates(name, 1000, 5, locationListener);

            }
        }
    }
    Address getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    void getCurrentWeather(double Letitude, double longitude) {
        APIService.getCurrentWeather(1, String.valueOf(latitude), String.valueOf(longitude)).enqueue(new Callback<WeatherInfo>() {
            @Override
            public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {
                if (response.body() != null) {
                    WeatherInfo Info = response.body();

                    Weather weather = Info.weather;

                    if (Info.weather.hourly.isEmpty() == false) {
                        Hourly hourly = Info.weather.hourly.get(0);
                        String temp = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";

                        setNotification(currentCity, hourly.sky.name, temp);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherInfo> call, Throwable t) {

            }
        });
    }
    
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void setNotification(String city, String sky, String temp) {
        Bundle bundle = mIntent.getExtras();

        if (bundle.getBoolean("isRepeat",false)) {
            NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0,
                    new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(R.mipmap.neighborhood_weather_icon)
                    .setContentTitle(city)
                    .setContentText(sky + ", " + temp)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent);


            Notification noti = builder.build();
            noti.flags |= Notification.FLAG_NO_CLEAR;

            manager.notify(1000, noti);
        } else  {
            NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0,
                    new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(R.mipmap.neighborhood_weather_icon)
                    .setContentTitle("날씨 알람")
                    .setContentText(city + ", " + sky + ", " + temp)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent);

            Notification noti = builder.build();

            manager.notify(1100, noti);
        }





    }
}
