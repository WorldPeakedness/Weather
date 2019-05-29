package android.coolweeather.com.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.coolweeather.com.coolweather.db.City;
import android.coolweeather.com.coolweather.db.County;
import android.coolweeather.com.coolweather.db.Province;
import android.coolweeather.com.coolweather.util.HttpUtil;
import android.coolweeather.com.coolweather.util.Utility;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2019/5/15.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectprovince;
    private City selectcity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectprovince=provinceList.get(i);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectcity=cityList.get(i);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(i).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof  WeatherActivity){
                        WeatherActivity weatherActivity=(WeatherActivity) getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.refreshLayout.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel==LEVEL_CITY){
                     queryProvinces();
                }else if(currentLevel==LEVEL_COUNTY){
                     queryCities();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address ="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        titleText.setText(selectprovince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceID=?",String.valueOf(selectprovince.getProvinceCode())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for(City city: cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            String address ="http://guolin.tech/api/china/"+selectprovince.getProvinceCode()+"\"";
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectcity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",String.valueOf(selectcity.getCityCode())).find(County.class);
        if(countyList.size()>0){
            datalist.clear();
            for(County County: countyList){
                datalist.add(County.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            String address ="http://guolin.tech/api/china/"+selectprovince.getProvinceCode()+"/"+selectcity.getCityCode()+"\"";
            queryFromServer(address,"county");
        }
    }

    private void ShowProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getContext());
            progressDialog.setMessage("加载中..........");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void CloseProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void queryFromServer(String address,final String type){
        ShowProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CloseProgressDialog();
                        Toast.makeText(getContext(),"加载失败..",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean  result=false;
                if("province".equals(type)){
                    result=Utility.handleProvinceResponse(response.body().string());
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(response.body().string(),selectprovince.getProvinceCode());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(response.body().string(),selectcity.getCityCode());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CloseProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
}
