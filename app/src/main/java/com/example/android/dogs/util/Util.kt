package com.example.android.dogs.util

import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.dogs.R

/**
 * author: RanaSiroosian
 */
val PERMISSION_SEND_SMS = 234

/**The method below give us the spinner on the screen while the actual url
 * images being loaded
 */
fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

/**Extension function for ImageView element,
 * and we use Glide to load the url to the ImageView
 */
fun ImageView.loadImage(uri: String?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        //If there is any error then display the default image
        .placeholder(progressDrawable).error(R.mipmap.ic_dog_icon)

    Glide.with(context).applyDefaultRequestOptions(options)
        //'this' in the code below refers to the ImageView here
        .load(uri).into(this)

}

/**The binding adapter simply make a function available to our layout. This function is using the
 * extension function that we made (loadImage) to load a url into an imageView.
 */
@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.loadImage(url, getProgressDrawable(view.context))
}