package kr.hs.buil.neighborhoodweather;

import kr.hs.buil.neighborhoodweather.model.forecast.ForecastInfo;
import kr.hs.buil.neighborhoodweather.model.weather.WeatherInfo;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by user on 2017-09-10.
 */

public interface WeatherService {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://apis.skplanetx.com/weather/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    @Headers("appKey:" + Data.AppKey)
    @GET("current/hourly?")
    Call<WeatherInfo> getCurrentWeather(@Query("version")int version, @Query("lat") String lat, @Query("lon") String lon);
    @Headers("appKey:"+ Data.AppKey)
    @GET("forecast/3days?")
    Call<ForecastInfo> getForecast3Days(@Query("version")int version, @Query("lat")String lat, @Query("lon") String lon);
}
