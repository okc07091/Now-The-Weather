package kr.hs.buil.neighborhoodweather;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import kr.hs.buil.neighborhoodweather.model.forecast.Forecast3day;
import kr.hs.buil.neighborhoodweather.model.forecast.ForecastInfo;
import kr.hs.buil.neighborhoodweather.model.weather.Hourly;
import kr.hs.buil.neighborhoodweather.model.weather.WeatherInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_PERMISSION = 1000;
    WeatherService APIService;
    LocationManager locationManager = null;
    LocationListener locationListener = null;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    double latitude = 0.0;
    double longitude = 0.0;
    ArrayList<String> cities = new ArrayList<>();
    String currentCity = "";
    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.tvSky)
    TextView tvSky;
    @BindView(R.id.tvTemperNow)
    TextView tvTemperNow;
    @BindView(R.id.tvTemperMin)
    TextView tvTemperMin;
    @BindView(R.id.tvTemperMax)
    TextView tvTemperMax;
    @BindView(R.id.layoutTemperature)
    LinearLayout layoutTemperature;
    @BindView(R.id.layoutBackground)
    LinearLayout layoutBackground;
    @BindView(R.id.rvForecast)
    RecyclerView rvForecast;
    @BindView(R.id.lvCity)
    ListView lvCity;
    @BindView(R.id.btnAlarm)
    ImageView btnAlarm;

    ForecastAdapter mForecastAdapter;

    AlarmManager mAlarmManager;
    long mAlarmTriggerTime = 0;

    boolean isExiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        APIService = WeatherService.retrofit.create(WeatherService.class);

        SharedPreferences sharedPref = getSharedPreferences("com.kimjunu.neighborhoodweather", Context.MODE_PRIVATE);
        mAlarmTriggerTime = sharedPref.getLong("alarm_trigger_time", 0);

        if (mAlarmTriggerTime == 0)
            btnAlarm.setImageResource(R.drawable.ic_add_alarm_black_24dp);
        else
            btnAlarm.setImageResource(R.drawable.ic_alarm_on_black_24dp);

        boolean isGranted = checkLocationPermission();

        if (isGranted) {
            initLocationManager();
            getLocationInfo();
        }
    }

    @Override
    public void onBackPressed() {
        if (isExiting == false) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExiting = false;
                }
            },2000);
            Toast.makeText(this, "앱을 종료하려면 '뒤로' 버튼을 한 번 더 누르세요.", Toast.LENGTH_SHORT).show();

            isExiting = true;
        }else {
            finish();
        }
    }

    boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
            return false;
        }
        return true;
    }

    void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

                    currentCity = city;

                    locationManager.removeUpdates(locationListener);
                    currentCity = city;
                    tvLocation.setText(currentCity);

                    if(cities.contains(currentCity)==false)
                        cities.add(currentCity);

                    ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1, cities);
                    lvCity.setAdapter(adapter);

                    getCurrentWeather(latitude, longitude);
                    getForecast3Days(latitude, longitude);
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
        } else {
            Toast.makeText(this, "Gps, Network설정이 꺼져있습니다. 설정화면으로 넘어갑니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    Address getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.KOREA);
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
    Address getLocationFromAddress(String addressString){
        Geocoder geocoder = new Geocoder(MainActivity.this);
        Address address = null;

        try {
            List<Address> addresses
                    = geocoder.getFromLocationName(addressString, 5);
            if (addresses.isEmpty() == false)
                address = addresses.get(0);
        } catch(IOException e){
            e.printStackTrace();
        }
        return address;
    }
    void getForecast3Days(double latitude, double longitude){
        APIService.getForecast3Days(1, String.valueOf(latitude), String.valueOf(longitude))
                .enqueue(new Callback<ForecastInfo>() {
                    public ForecastAdapter mForecastAdapter;

                    @Override
                    public void onResponse(Call<ForecastInfo> call, Response<ForecastInfo> response) {
                        if (response.body() == null)
                            return;
                        ForecastInfo info = response.body();

                        if (info.weather == null || info.weather.forecast3days.isEmpty())
                            return;

                        ArrayList<SimpleForecast> forecastInfo = new ArrayList<>();

                        int offsetHour = 3;
                        int maxHour = 40;

                        Date today = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 a hh시");

                        for (int hour = 4; hour <= maxHour; hour += offsetHour) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(today);
                            calendar.add(Calendar.HOUR, hour);

                            String time = sdf.format(calendar.getTime());
                            String sky = "";
                            String temp = "";

                            try{
                                Forecast3day forecast = info.weather.forecast3days.get(0);

                                Field skyField = forecast.fcst3hour.sky.getClass()
                                        .getField("name"+hour+"hour");
                                sky = skyField.get(forecast.fcst3hour.sky).toString();

                                Field tempField = forecast.fcst3hour.temperature.getClass()
                                        .getField("temp"+hour+"hour");
                                temp = tempField.get(forecast.fcst3hour.temperature).toString();

                                if (temp.isEmpty() == false)
                                    temp = String.valueOf((int) Double.parseDouble(temp)) + "℃";
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            forecastInfo.add(new SimpleForecast(time, sky, temp));
                        }
                        rvForecast.setHasFixedSize(true);

                        mForecastAdapter = new ForecastAdapter(forecastInfo);
                        rvForecast.setAdapter(mForecastAdapter);

                        mForecastAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ForecastInfo> call, Throwable t) {

                    }
                });
    }

    void getCurrentWeather(double Letitude, double longitude) {
    APIService.getCurrentWeather(1, String.valueOf(latitude), String.valueOf(longitude)).enqueue(new Callback<WeatherInfo>() {
        @Override
        public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {
            if (response.body() != null) {
                WeatherInfo Info = response.body();


                if (Info.weather.hourly.isEmpty() == false) {
                    Hourly hourly = Info.weather.hourly.get(0);
                    tvSky.setText(hourly.sky.name);
                    String temp = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";
                    String tempMin = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";
                    String tempMax = ((int) Double.parseDouble(hourly.temperature.tc)) + "℃";
                    tvTemperMax.setText(temp);
                    tvTemperMin.setText(tempMin);
                    tvTemperNow.setText(tempMax);
                    layoutTemperature.setVerticalGravity(View.VISIBLE);

                    View rootView = getWindow().getDecorView().getRootView();

                    if (hourly.sky.code.equals("SKY_O01")) {
                        rootView.setBackgroundResource(R.mipmap.bg_sunny);
                    }else if (hourly.sky.code.equals("SKY_O02") || hourly.sky.code.equals("SKY_O03") ||
                    hourly.sky.code.equals("SKY_O07")) {
                        rootView.setBackgroundResource(R.mipmap.bg_cloudy);
                    } else if (hourly.sky.code.equals("SKY_O04") || hourly.sky.code.equals("SKY_O06") ||
                            hourly.sky.code.equals("SKY_O08") || hourly.sky.code.equals("SKY_O10")) {
                        rootView.setBackgroundResource(R.mipmap.bg_rainy);
                    } else if (hourly.sky.code.equals("SKY_O05") || hourly.sky.code.equals("SKY_O09")){
                        rootView.setBackgroundResource(R.mipmap.bg_snowy);
                    } else {
                        rootView.setBackgroundResource(R.mipmap.bg_lightening);
                    }

                    Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                    intent.putExtra("isRepeat",true);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (mAlarmManager == null)
                        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR,
                            pendingIntent);

                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0,
                            new Intent(MainActivity.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification.Builder builder = new Notification.Builder(MainActivity.this);
                    builder.setSmallIcon(R.mipmap.neighborhood_weather_icon)
                            .setContentTitle(currentCity)
                            .setContentText(hourly.sky.name + ", " + temp)
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pIntent);

                    builder.setPriority(Notification.PRIORITY_MAX);

                    Notification noti = builder.build();
                    noti.flags |= Notification.FLAG_NO_CLEAR;

                    manager.notify(1000, noti);
                }
            }
        }

        @Override
        public void onFailure(Call<WeatherInfo> call, Throwable t) {

        }
    });
    }
    @OnClick(R.id.btnAddCity)
    void showAddressDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText etAddress = new EditText(this);

        etAddress.setSingleLine();
        alert.setTitle("지역 추가");
        alert.setMessage("주소를 입력하세요\n(ex: oo시 oo구 oo동");
        alert.setView(etAddress);


        alert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String address = etAddress.getText().toString();

                        Address location = getLocationFromAddress(address);

                        if (address == null)
                            return;
                        String city;
                        String[] temp = location.getAddressLine(0).split(" ");

                        if (temp.length >= 3) {
                            if (temp[0].equals("대한민국"))
                                city = temp[1] + " " + temp[2] + " " + temp[3];
                            else
                                city = temp[0] + " " + temp[1] + " " + temp[2];
                        } else {
                            city = location.getAddressLine(0);
                        }
                        if (cities.contains(city) == false)
                            cities.add(city);

                        if(location == null) {
                            Toast.makeText(MainActivity.this, "주소를 확인하세요.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        cities.add(location.getAddressLine(0));

                    }
                });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
                alert.create().show();
    }
    @OnItemClick(R.id.lvCity)
    void changeCity(AdapterView<?> parent, int position) {
        updateCityWeather(position);
    }
    public void updateCityWeather(int position) {
        Address location = getLocationFromAddress(cities.get(position));

        if (location == null)
            return;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        getCurrentWeather(latitude, longitude);
        getForecast3Days(latitude, longitude);

        tvLocation.setText(location.getAddressLine(0));
    }
    @OnItemLongClick(R.id.lvCity)
    public boolean onCityLongClick(AdapterView<?> parent, final int position) {
        if(position == 0)
            return true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("지역 삭제");
        builder.setMessage(cities.get(position) + "  을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cities.remove(position);
                ArrayAdapter adapter = (ArrayAdapter) lvCity.getAdapter();
                adapter.notifyDataSetChanged();

                updateCityWeather(0);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }
    @OnClick(R.id.btnAlarm)
    public void showSetAlarmDialog() {
        Calendar time = Calendar.getInstance();

        if (mAlarmTriggerTime == 0)
            time.setTimeInMillis(System.currentTimeMillis());
        else
            time.setTimeInMillis(mAlarmTriggerTime);

        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                cancelAlarm();

                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                long atime = System.currentTimeMillis();

                Calendar curTime = Calendar.getInstance();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    curTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    curTime.set(Calendar.MINUTE, timePicker.getMinute());
                } else {
                    curTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    curTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                }
                curTime.set(Calendar.SECOND, 0);
                curTime.set(Calendar.MILLISECOND, 0);

                long btime = curTime.getTimeInMillis();
                mAlarmTriggerTime = btime;

                if (atime > btime)
                    mAlarmTriggerTime += 1000 * 60 * 60 * 24;

                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mAlarmTriggerTime, AlarmManager.INTERVAL_DAY, pIntent);

                btnAlarm.setImageResource(R.drawable.ic_alarm_on_black_24dp);

                SharedPreferences sharedPref = getSharedPreferences("com.kimjunu.neighborhoodweather", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("alarm_trigger_time", mAlarmTriggerTime);
                editor.commit();
            }
        }, hour, minute, false);
        dialog.updateTime(hour, minute);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "해제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelAlarm();

                mAlarmTriggerTime = 0;

                btnAlarm.setImageResource(R.drawable.ic_add_alarm_black_24dp);

                SharedPreferences sharedPref = getSharedPreferences("com.kimjunu.neighborhoodweather", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("alarm_trigger_time", mAlarmTriggerTime);
                editor.commit();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "알람 설정", dialog);

        dialog.show();
    }

    public void cancelAlarm() {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.cancel(pIntent);
    }
}