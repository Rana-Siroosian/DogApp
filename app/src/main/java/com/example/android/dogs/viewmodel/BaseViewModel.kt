package com.example.android.dogs.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
/**
 * author: RanaSiroosian
 */
/**We use AndroidViewModel here because it takes an application context and our database needs
 * a context rather than a regular activity context because that is really volatile and it can be
 * destroyed on and out.
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application)
    , CoroutineScope {

    private val job = Job()

    override val coroutineContext : CoroutineContext
        //After job is finished running we're going to return to the Main thread
    get() = job + Dispatchers.Main

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }


}