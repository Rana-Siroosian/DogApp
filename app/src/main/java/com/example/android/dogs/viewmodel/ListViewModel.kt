package com.example.android.dogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.dogs.model.DogBreed
import com.example.android.dogs.model.DogDatabase
import com.example.android.dogs.model.DogsApiService
import com.example.android.dogs.util.NotificationsHelper
import com.example.android.dogs.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

/**
 * author: RanaSiroosian
 */
class ListViewModel(application: Application) : BaseViewModel(application) {

    private var prefHelper = SharedPreferencesHelper(getApplication())

    /**If the data has been retrieved over than 5 minutes ago then we will get the data from the
     * endpoint but if it's less than 5 minutes ago then we will get it from the database. In order
     * to do that we make the refreshTime (this is the time after which the data needs to be refreshed.
     * We need to put this in nanosecond and the reason is because we saved out update time in
     * nanosecond. 5 * 60 seconds * 1000 milli seconds * 1000 micro seconds * 1000 nano seconds. We
     * need to store everything in a Long variable type
     */
    private var refreshTime = 5 * 60 * 1000 * 1000 * 1000L
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
    fun refresh() {
        checkCacheDuration()
        val updateTime = prefHelper.getUpdateTime()
        //Here we need to check and see if it's more than 5 minutes ago or not
        if (updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    private fun checkCacheDuration() {
        val cachePreference = prefHelper.getCacheDuration()

        try {
            val cachePreferenceInt = cachePreference?.toInt() ?: 5 * 60
            //refreshTime will be updated based on the preferences that the user set
            refreshTime = cachePreferenceInt.times(1000 * 1000 * 1000L)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    //Every time we refresh, we would get the data from the endpoint
    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromDatabase() {
        loading.value = true
        launch {
            val dogs = DogDatabase(getApplication()).dogDao().getAllDogs()
            dogsRetrieved(dogs)
            Toast.makeText(getApplication(), "Dogs retrieved from database", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchFromRemote() {
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
                .subscribeWith(object : DisposableSingleObserver<List<DogBreed>>() {
                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogsLocally(dogList)
                        Toast.makeText(
                            getApplication(),
                            "Dogs retrieved from endpoint",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        NotificationsHelper(getApplication()).createNotification()

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

    private fun dogsRetrieved(dogList: List<DogBreed>) {
        //We get the dogList
        dogs.value = dogList
        dogsLoadError.value = false
        loading.value = false
    }

    private fun storeDogsLocally(list: List<DogBreed>) {
        launch {
            val dao = DogDatabase(getApplication()).dogDao()

            /**We deleteAllDogs because for example if we arrived here the second time and we want
             * to store dogs locally we don't want our database polluted with the previous dogs
             */
            dao.deleteAllDogs()

            /**The code below gets a list and it expands it to an individual element that we can
             * pass to our insertAll function in Dao class and we retrieve a list of uuid
             */
            val result = dao.insertAll(*list.toTypedArray())


            var i = 0
            while (i < list.size) {
                //We assign the corresponding i to the corresponding list element
                list[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(list)

        }

        /**Stores the information exactly the moment when we have updated the database with the dog
         * information that we retrieved and we will use this information when we decide whether we
         * should retrieve the data from database or refresh the data from the remote endpoint
         */
        prefHelper.saveUpdateTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}