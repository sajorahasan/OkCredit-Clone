package com.sajorahasan.okcredit.utils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.io.IOException
import java.util.*


class FetchAddressIntentService : IntentService("GetAddressService") {
    private val TAG = "FetchAddressIntentSv"
    private var receiver: ResultReceiver? = null


    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        var msg = ""
        //get result receiver from intent
        receiver = intent.getParcelableExtra(Constants.RECEIVER)!!
        val location :Location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA)!!

        if (receiver == null) {
            Log.e(TAG, "No receiver, not processing the request further")
            return
        }

        //call GeoCoder getFromLocation to get address
        //returns list of addresses, take first one and send info to result receiver
        val geoCoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address> = emptyList()

        try {
            addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: IOException) {

            Log.e(TAG, "Error in getting address for the location $ioException")
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            msg = "No address found for the location"
            deliverResultToReceiver(Constants.FAILURE_RESULT, msg)
        } else {
            val address = addresses[0]
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            val addressFragments = with(address) {
                (0..maxAddressLineIndex).map { getAddressLine(it) }
            }
            Log.i(TAG, "Address Found")
            deliverResultToReceiver(
                Constants.SUCCESS_RESULT,
                addressFragments.joinToString(separator = "\n")
            )
        }
    }


    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(Constants.RESULT_DATA_KEY, message) }
        receiver?.send(resultCode, bundle)
    }

}