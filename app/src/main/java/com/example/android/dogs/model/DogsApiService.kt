package com.example.android.dogs.model

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * author: RanaSiroosian
 */
class DogsApiService {

    /**The service is going to match the base url with the endpoint
     * of the method that we defined
     */
    private val BASE_URL = "https://raw.githubusercontent.com"

    private val api = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(DogsApi::class.java)

    fun getDogs(): Single<List<DogBreed>> {
        return api.getDogs()
    }

}