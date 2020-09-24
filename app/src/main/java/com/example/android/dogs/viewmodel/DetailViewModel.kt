package com.example.android.dogs.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.dogs.model.DogBreed

class DetailViewModel : ViewModel() {

    val dogLiveData = MutableLiveData<DogBreed>()

    fun fetch(){

        val dog1 = DogBreed("1","Husky","15 years",
            "breedGroup","bredFor","temperament","")

        dogLiveData.value = dog1

    }
}