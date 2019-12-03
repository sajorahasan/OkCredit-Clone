package com.sajorahasan.okcredit.activity.navigationActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.model.User
import com.sajorahasan.okcredit.room.KasbonRoomDb
import com.sajorahasan.okcredit.utils.Constants
import com.sajorahasan.okcredit.utils.FetchAddressIntentService
import com.sajorahasan.okcredit.utils.Prefs
import com.sajorahasan.okcredit.utils.Tools
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity() {

    companion object {
        const val TAG = "ProfileActivity"
        const val LOCATION_PERMISSION_REQUEST_CODE = 2
        const val GPS_SETTINGS_REQUEST_CODE = 52
    }

    private lateinit var user: User
    private lateinit var db: KasbonRoomDb
    private var disposable: CompositeDisposable? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var locationManager: LocationManager? = null
    private lateinit var resultReceiver: AddressResultReceiver

    private var addressOutput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Initialize views
        initViews()

        // set profile data
        setProfileData()
    }

    private fun initViews() {
        toolbar.title = "Profile"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        disposable = CompositeDisposable()
        db = KasbonRoomDb.getDatabase(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        resultReceiver = AddressResultReceiver(Handler())

        camera.setOnClickListener { requestStoragePermission() }
        addressIcon.setOnClickListener {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    currentLocation = locationResult?.locations?.get(0)
                    Log.d(TAG, "Current Location Latitude: $currentLocation")
                    getAddress()
                }
            }
            startLocationUpdates()
        }
        tvName.setOnClickListener { openNameEditText() }
        tvAddress.setOnClickListener { openAddressEditText() }
        tvAbout.setOnClickListener { openCommonEditText("about") }
        tvEmail.setOnClickListener { openCommonEditText("email") }
        tvContactName.setOnClickListener { openCommonEditText("contact") }
    }

    private fun setProfileData() {
        user = intent.getParcelableExtra("user")!!
        Log.d(TAG, "User===> $user")

        if (user.profileImage.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfile)
        }

        tvName.text = user.name
        tvAddress.text = user.businessAddress
        tvAbout.text = user.businessDesc
        tvEmail.text = user.email
        tvContactName.text = user.contactPersonName
        tvPhone.text = user.phone
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val locationRequest = LocationRequest()
                //locationRequest.interval = 2000
                //locationRequest.fastestInterval = 1000
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } else {
                val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
                builder.setTitle(getString(R.string.turnon_location))
                builder.setMessage(getString(R.string.location_permission_msg))
                builder.setPositiveButton(getString(R.string.location_settings)) { dialog, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, 52)
                    dialog.dismiss()
                }
                builder.setNegativeButton(getString(R.string.cancel_tag)) { dialog, _ -> dialog.dismiss() }
                builder.show()
            }
        }
    }

    private fun getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(
                this,
                "Can't find current address, ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.RECEIVER, resultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation)
        }
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult $requestCode $permissions $grantResults")
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(
                        this, "Location permission not granted, " +
                                "restart the app if you want the feature",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

            if (resultCode == 0) {
                //Last Location can be null for various reasons
                //for example the api is called first time
                //so retry till location is set
                //since intent service runs on background thread, it doesn't block main thread
                Log.d(TAG, "Location null retrying")
                //getAddress()
            }

            if (resultCode == 1) {
                Toast.makeText(
                    this@ProfileActivity, "Address not found.", Toast.LENGTH_SHORT
                ).show()
            }


            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData?.getString(Constants.RESULT_DATA_KEY) ?: ""
            Log.d(TAG, "addressOutput -- $addressOutput")
            if (addressOutput.length > 10) {
                tvAddress.text = addressOutput
                user.businessAddress = addressOutput
                updateUser()
            }


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(
                    this@ProfileActivity, "Address Found - $addressOutput", Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $requestCode $resultCode $data")
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) run {
                //for getting image from Camera
                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(
//                        this.contentResolver,
//                        Uri.parse(mCurrentPhotoPath)
//                    )
//                    bitmap =
//                        rotateImageIfRequired(bitmap!!, Uri.parse(mCurrentPhotoPath).path!!)
//                    ivProfile.setImageBitmap(bitmap)
//                    ivProfile.visibility = View.VISIBLE

                    Glide.with(this)
                        .load(Uri.parse(mCurrentPhotoPath).path)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfile)

                    user.profileImage = Uri.parse(mCurrentPhotoPath).path.toString()
                    updateUser()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else
                if (requestCode == SELECT_FILE) run {
                    //for getting image from file
                    val uri = data!!.data
                    val type = uri?.let { getMimeType(this, it) }
                    if (type != null && type.isNotEmpty() && type != "null") {
                        if (type.toLowerCase().contains("jpg")
                            || type.toLowerCase().contains("png")
                            || type.toLowerCase().contains("jpeg")
                        ) {

                            Log.d(TAG, "uri -- $uri")

                            Glide.with(this)
                                .load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivProfile)

                            user.profileImage = uri.toString()
                            updateUser()

                        } else {
                            Toast.makeText(this, "Please provide valid image.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Please provide valid image.", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else
                    if (requestCode == 101) run {
                        //for runtime permission
                        requestStoragePermission()
                    }
        }

        if (requestCode == GPS_SETTINGS_REQUEST_CODE) {
            startLocationUpdates()
        }
    }

    private fun openNameEditText() {
        btnShare.visibility = View.GONE
        nameContainer.visibility = View.VISIBLE

        //Initialize editText's views
        val etName = nameContainer.findViewById<AppCompatEditText>(R.id.etName)
        val btnSubmitNameContainer =
            nameContainer.findViewById<FrameLayout>(R.id.btnSubmitNameContainer)
        val btnSubmitName = nameContainer.findViewById<FloatingActionButton>(R.id.btnSubmitName)

        etName.setText(user.name)
        Tools.showKeyboard(etName)
        etName.requestFocus()
        etName.afterTextChanged { s ->
            if (s.length > 2) {
                btnSubmitNameContainer.visibility = View.VISIBLE
            } else {
                btnSubmitNameContainer.visibility = View.GONE
            }
        }

        btnSubmitName.setOnClickListener {
            user.name = etName.text.toString().trim()
            updateUser()
        }
    }

    private fun openAddressEditText() {
        btnShare.visibility = View.GONE
        addressContainer.visibility = View.VISIBLE

        //Initialize editText's views
        val etAddress = addressContainer.findViewById<AppCompatEditText>(R.id.etAddress)
        val btnSubmitAddressContainer =
            addressContainer.findViewById<FrameLayout>(R.id.btnSubmitAddressContainer)
        val btnSubmitAddress =
            addressContainer.findViewById<FloatingActionButton>(R.id.btnSubmitAddress)

        etAddress.setText(user.businessAddress)
        Tools.showKeyboard(etAddress)
        etAddress.requestFocus()
        etAddress.afterTextChanged { s ->
            if (s.length > 2) {
                btnSubmitAddressContainer.visibility = View.VISIBLE
            } else {
                btnSubmitAddressContainer.visibility = View.GONE
            }
        }

        btnSubmitAddress.setOnClickListener {
            user.businessAddress = etAddress.text.toString().trim()
            updateUser()
        }
    }

    private fun openCommonEditText(type: String) {
        btnShare.visibility = View.GONE
        commonContainer.visibility = View.VISIBLE

        //Initialize editText's views
        val etCommon = commonContainer.findViewById<AppCompatEditText>(R.id.etCommon)
        val btnSubmitCommonContainer =
            commonContainer.findViewById<FrameLayout>(R.id.btnSubmitCommonContainer)
        val btnSubmitCommon =
            commonContainer.findViewById<FloatingActionButton>(R.id.btnSubmitCommon)

        when (type) {
            "about" -> {
                etCommon.inputType = InputType.TYPE_CLASS_TEXT
                etCommon.setHint(R.string.aboutBusiness)
                etCommon.setText(user.businessDesc)
            }
            "email" -> {
                etCommon.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                etCommon.setHint(R.string.email)
                etCommon.setText(user.email)
            }
            "contact" -> {
                etCommon.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                etCommon.setHint(R.string.contact_person_name)
                etCommon.setText(user.contactPersonName)
            }
        }

        Tools.showKeyboard(etCommon)
        etCommon.requestFocus()
        etCommon.afterTextChanged { s ->
            if (s.length > 2) {
                btnSubmitCommonContainer.visibility = View.VISIBLE
            } else {
                btnSubmitCommonContainer.visibility = View.GONE
            }
        }

        btnSubmitCommon.setOnClickListener {
            val s = etCommon.text.toString().trim()
            when (type) {
                "about" -> {
                    user.businessDesc = s
                }
                "email" -> {
                    user.email = s
                }
                "contact" -> {
                    user.contactPersonName = s
                }
            }

            updateUser()
        }
    }

    private fun getUserFromDb(): User {
        val phone = Prefs.getString("phone")
        return db.userDao().getUser(phone!!)
    }

    private fun updateDb(): User {
        db.userDao().updateUser(user)
        return getUserFromDb()
    }

    private fun updateUser() {
        disposable!!.add(
            Single.create<User> { e -> e.onSuccess(updateDb()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userDataUpdateSuccess() }) { handleError(it) }
        )
    }

    private fun userDataUpdateSuccess() {
        Tools.hideKeyboard(nameContainer)
        nameContainer.visibility = View.GONE
        addressContainer.visibility = View.GONE
        commonContainer.visibility = View.GONE
        btnShare.visibility = View.VISIBLE

        setProfileData()
    }

    private fun handleError(t: Throwable?) {
        Log.d(TAG, "handleError:  ${t?.localizedMessage}")
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy FROM PROFILE")
        if (locationCallback != null && fusedLocationClient != null)
            fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

}
