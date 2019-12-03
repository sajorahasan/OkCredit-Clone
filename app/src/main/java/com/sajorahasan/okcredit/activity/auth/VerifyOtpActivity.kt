package com.sajorahasan.okcredit.activity.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.activity.HomeActivity
import com.sajorahasan.okcredit.api.RestAPI
import com.sajorahasan.okcredit.api.RestAdapter
import com.sajorahasan.okcredit.api.callback.OtpResponse
import com.sajorahasan.okcredit.model.User
import com.sajorahasan.okcredit.room.KasbonRoomDb
import com.sajorahasan.okcredit.utils.Prefs
import com.sajorahasan.okcredit.utils.Tools
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_verify_otp.*


class VerifyOtpActivity : AppCompatActivity() {
    private var tag = "VerifyOtpActivity"
    private var mobile: String? = null

    private lateinit var db: KasbonRoomDb

    private var api: RestAPI? = null
    private var disposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        //Initialize views
        initViews()
    }

    private fun initViews() {
        mobile = intent.getStringExtra("mobile")

        tvOtpDelivery.text = getString(R.string.your_otp_will_be_delivered, mobile)

        disposable = CompositeDisposable()
        api = RestAdapter.getInstance()
        db = KasbonRoomDb.getDatabase(this)

        pinViewOtp.setPinViewEventListener { pinView, _ ->
            Tools.hideKeyboard(pinView)
            //verifyOtp(pinView.value)
            if (pinView.value == "123456"){
                saveUser()
            }else {
                Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyOtp(otp: String) {
        disposable!!.add(
            api!!.verifyUser(mobile!!, otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .doFinally { hideLoader() }
                .subscribe({ handleSuccess(it) }) { handleError(it) }
        )
    }

    private fun showLoader() {
        loader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        loader.visibility = View.GONE
    }

    private fun handleSuccess(data: OtpResponse) {
        Log.d(tag, "handleSuccess $data")

        if (data.validOTP) {
            saveUser()
        } else {
            Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show()
        }

    }


    private fun handleError(t: Throwable?) {
        Log.d(tag, "handleError:  ${t?.localizedMessage}")
        Toast.makeText(this, "Error : ${t?.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    private fun saveUserToDb(): User {
        db.userDao().insert(User(phone = mobile))
        return db.userDao().getUser(mobile!!)
    }

    private fun saveUser() {
        disposable!!.add(
            Single.create<User> { e -> e.onSuccess(saveUserToDb()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(tag, "On Success: Data Written Successfully! $it")
                    Prefs.save("phone", it.phone)
                    gotoHome()
                }) { Log.d(tag, "On Error:  $it") }
        )
    }

    private fun gotoHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}
