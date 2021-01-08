package com.example.api;

import com.example.models.DataModel;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;

public interface ApiServices {
    @GET("random")
    Call<DataModel> getData();

}
