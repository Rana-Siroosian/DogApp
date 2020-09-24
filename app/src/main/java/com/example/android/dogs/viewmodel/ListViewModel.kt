package com.example.android.dogs.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.dogs.model.DogBreed
import com.example.android.dogs.model.DogsApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ListViewModel :ViewModel(){

    private val dogsService = DogsApiService()

    /**This allows us to avoid any memory licking while our application
     * has been destroyed and we manage that with onCleared method
     */
    private val disposable = CompositeDisposable()
    /**It will provide the information for the actual list of dogs
     * that we retrieve from our data source which can be from the API
     * or a local database
     */
    val dogs = MutableLiveData<List<DogBreed>>()

    /**This LiveData will notify whoever is listening to this viewModel
     *that there is an error
     */
    val dogsLoadError = MutableLiveData<Boolean>()

    /**It notifies that the system is loading, the data has not arrived
     *but we don't have any error
     */
    val loading = MutableLiveData<Boolean>()



    //This function simply refresh the information
    fun refresh(){
       fetchFromRemote()
    }

    private fun fetchFromRemote(){
        loading.value = true
        disposable.add(
            /**This will return the single that we defined, we also have to set it on a different
             * thread rather than being on the API thread because we don't want to block the
             * application while the call is finished, so we pass this call to the endpoint of a
             * background thread
             */
            dogsService.getDogs().subscribeOn(Schedulers.newThread())
                /**The result of the process has to be on the main thread of our application and
                 *in order to dlo that we use the observeOn method
                 */
                .observeOn(AndroidSchedulers.mainThread())
                    //Then we pass the observer that we want to observe the single
                .subscribeWith(object : DisposableSingleObserver<List<DogBreed>>(){
                    override fun onSuccess(dogList: List<DogBreed>) {
                        //We get the dogList
                        dogs.value = dogList
                        dogsLoadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        //We get the error
                        dogsLoadError.value = true
                        loading.value = false
                        e.printStackTrace()
                    }

                })
        )

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}