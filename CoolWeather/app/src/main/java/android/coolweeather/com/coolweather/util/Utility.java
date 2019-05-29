package android.coolweeather.com.coolweather.util;

import android.coolweeather.com.coolweather.db.City;
import android.coolweeather.com.coolweather.db.County;
import android.coolweeather.com.coolweather.db.Province;
import android.coolweeather.com.coolweather.gson.Weather;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2019/5/10.
 */

public class Utility {
    public static  boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray  allProvinces =new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return  true;

            }catch (JSONException  e){
                e.printStackTrace();
            }

        }
        return  false;

    }

    public static  boolean handleCityResponse(String response,int ProvinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray  allCities =new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceID(ProvinceId);
                    city.save();
                }
                return  true;

            }catch (JSONException  e){
                e.printStackTrace();
            }

        }
        return  false;

    }

    public static  boolean handleCountyResponse(String response,int CityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray  allCounties =new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(CityId);
                    county.save();
                }
                return  true;

            }catch (JSONException  e){
                e.printStackTrace();
            }

        }
        return  false;

    }

    public static Weather handleWeatherResponse(String response){
        try{
              JSONObject jsonObject=new JSONObject(response);
              JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
              String weatherContent=jsonArray.get(0).toString();
              return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}