package com.example.android.dogs.view

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.android.dogs.R
import com.example.android.dogs.databinding.FragmentDetailBinding
import com.example.android.dogs.databinding.SendSmsDialogBinding
import com.example.android.dogs.model.DogBreed
import com.example.android.dogs.model.DogPalette
import com.example.android.dogs.model.SmsInfo
import com.example.android.dogs.util.getProgressDrawable
import com.example.android.dogs.util.loadImage
import com.example.android.dogs.viewmodel.DetailViewModel
import com.example.android.dogs.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

/**
 * author: RanaSiroosian
 */
class DetailFragment : Fragment() {

    private lateinit var viewModel: DetailViewModel
    private var dogUuid = 0

    private lateinit var dataBinding: FragmentDetailBinding

    private var sendSmsStarted = false
    private var currentDog: DogBreed? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }
        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)

        viewModel.fetch(dogUuid)

        observeViewModel()


    }

    fun observeViewModel() {

        viewModel.dogLiveData.observe(this, Observer { dog ->
            //We need to maintain the current dog information for the time of sending sms or sharing
            currentDog = dog
            dog?.let {
                dataBinding.dog = dog

                //Setup the background color based on an average color of the picture
                it.imageUrl?.let {
                    setupBackgroundColor(it)
                }
            }
        })

    }

    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette ->
                        val intColor = palette?.vibrantSwatch?.rgb ?: 0
                        val myPalette = DogPalette(intColor)
                        dataBinding.palette = myPalette
                    }
                }

            })


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                /**In order to ask for a permission only activity can perform that action, means
                 * the activity can ask for the permission and not the fragment so we need to call
                 * a method on the activity to start the process of asking for permission
                 */
                (activity as MainActivity).checkSmsPermission()

            }
            R.id.action_share -> {
                /**This is the generic flag when one application wants to share information with
                 * some other application
                 */
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}"
                )
                intent.putExtra(Intent.EXTRA_STREAM, currentDog?.imageUrl)
                /**This will pop up a dialog that lets the user choose which application should handle
                 * this sharing functionality
                 */
                startActivity(Intent.createChooser(intent, "Share with"))

            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**This is the method that will be called when the activity finishes and gets the result
     * whether that result is true or false, we will get the result here
     */
    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                val smsInfo = SmsInfo(
                    "", "${currentDog?.dogBreed} bred for ${currentDog?.bredFor}"
                    , currentDog?.imageUrl
                )
                val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(
                    LayoutInflater.from(it),
                    R.layout.send_sms_dialog, null, false
                )

                AlertDialog.Builder(it).setView(dialogBinding.root).setPositiveButton(
                    "Send SMS"
                ) { dialog, which ->
                    if (!dialogBinding.smsDestination.text.isNullOrEmpty()) {
                        smsInfo.to = dialogBinding.smsDestination.text.toString()
                        sendSms(smsInfo)
                    }
                }.setNegativeButton("Cancel") { dialog, which ->
                }.show()

                dialogBinding.smsInfo = smsInfo
            }
        }


    }

    private fun sendSms(smsInfo: SmsInfo) {

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)
    }
}