
package kr.hs.buil.neighborhoodweather.model.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Grid {

    @SerializedName("longitude")
    @Expose
    public String longitude;
    @SerializedName("latitude")
    @Expose
    public String latitude;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("county")
    @Expose
    public String county;
    @SerializedName("village")
    @Expose
    public String village;

}
