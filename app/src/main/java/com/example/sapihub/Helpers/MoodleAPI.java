package com.example.sapihub.Helpers;

import com.example.sapihub.Model.Token;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MoodleAPI {
    String BASE_URL = "https://moodle.sapidoc.ms.sapientia.ro/";
    String service = "moodle_mobile_app";

    @GET("login/token.php")
    Call<Token> loginUser(@Query("username") String username,
                          @Query("password") String password,
                          @Query("service") String service);
}
