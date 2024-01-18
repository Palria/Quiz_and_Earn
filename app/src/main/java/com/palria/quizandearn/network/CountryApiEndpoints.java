package com.palria.quizandearn.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CountryApiEndpoints {

    @GET("/json")
    Call<CountryResponseDataModel> getCurrentCountry();
}
