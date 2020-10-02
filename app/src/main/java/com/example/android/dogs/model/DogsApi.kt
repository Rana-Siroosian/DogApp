package com.example.android.dogs.model

import io.reactivex.Single
import retrofit2.http.GET

/**
 * author: RanaSiroosian
 */
interface DogsApi {

    /**We use the endpoint to retrieve all the information from Json
     *and return that as a single list
     */
    @GET("DevTides/DogsApi/master/dogs.json")
    fun getDogs(): Single<List<DogBreed>>
}