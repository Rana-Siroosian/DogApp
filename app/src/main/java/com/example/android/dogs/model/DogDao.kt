package com.example.android.dogs.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import retrofit2.http.DELETE

@Dao
interface DogDao {

    /**We use suspend to put it in the background thread where it can be
     * run whenever there is processing power available to run this function
     */
    @Insert
    suspend fun insertAll(vararg dogs: DogBreed): List<Long>

    @Query("SELECT * FROM dogbreed")
    suspend fun getAllDogs(): List<DogBreed>

    @Query("SELECT * FROM dogbreed WHERE uuid = :dogId")
    suspend fun getDog(dogId: Int): DogBreed

    @Query("DELETE FROM dogbreed")
    suspend fun deleteAllDogs()
}