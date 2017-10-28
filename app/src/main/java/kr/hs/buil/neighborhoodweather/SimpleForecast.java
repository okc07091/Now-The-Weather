package kr.hs.buil.neighborhoodweather;

/**
 * Created by user on 2017-09-16.
 */

public class SimpleForecast {
    public String time;
    public String sky;
    public String temperature;

    SimpleForecast(String time, String sky, String temperature){
        this.time = time;
        this.sky = sky;
        this.temperature = temperature;
    }
}
