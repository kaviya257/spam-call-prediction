package com.example.spam_new;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit getClient() {

        if (retrofit == null) {

            // ============================
            // LONG TIMEOUT CLIENT
            // ============================

            OkHttpClient okHttpClient =
                    new OkHttpClient.Builder()

                            .connectTimeout(120, TimeUnit.SECONDS)

                            .readTimeout(120, TimeUnit.SECONDS)

                            .writeTimeout(120, TimeUnit.SECONDS)

                            .build();

            // ============================
            // RETROFIT
            // ============================

            retrofit = new Retrofit.Builder()

                    .baseUrl("http://10.66.238.184:8000/")

                    .client(okHttpClient)

                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )

                    .build();
        }

        return retrofit;
    }
}