package com.sajorahasan.okcredit.activity.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.api.RestAPI
import com.sajorahasan.okcredit.api.RestAdapter
import com.sajorahasan.okcredit.utils.Tools
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.ResponseBody
import java.util.*


class LoginActivity : BaseActivity() {
    private var tag = "LoginActivity"

    private lateinit var mobileNo: String

    private var api: RestAPI? = null
    private var disposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Initialize views
        initViews()
    }

    private fun initViews() {

        val locale: String = Locale.getDefault().country
        Log.d(tag, "countryID $locale")

        disposable = CompositeDisposable()
        api = RestAdapter.getInstance()

        tvPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim().length == 10) {
                    btnOK.backgroundTintList =
                        ContextCompat.getColorStateList(this@LoginActivity, R.color.green_dark)
                    btnOK.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.white))
                    btnOK.iconTint =
                        ContextCompat.getColorStateList(this@LoginActivity, R.color.white)
                } else {
                    btnOK.backgroundTintList =
                        ContextCompat.getColorStateList(this@LoginActivity, R.color.white)
                    btnOK.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.black_88))
                    btnOK.iconTint =
                        ContextCompat.getColorStateList(this@LoginActivity, R.color.black_88)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        btnOK.setOnClickListener {
            mobileNo = tvPhone.text.toString().trim()
            if (Tools.isValidMobile(mobileNo)) {
                Tools.hideKeyboard(tvPhone)
                //requestOtp()
                goToVerifyScreen()
            } else {
                Toast.makeText(this, getString(R.string.invalid_mobile), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestOtp() {
        disposable!!.add(
            api!!.createUser("91", mobileNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .doFinally { hideLoader() }
                .subscribe({ handleSuccess(it) }) { handleError(it) }
        )
    }

    private fun showLoader() {
        btnOK.visibility = View.GONE
        loader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        loader.visibility = View.GONE
        btnOK.visibility = View.VISIBLE
    }

    private fun handleSuccess(body: ResponseBody) {
        Log.d(tag, "handleSuccess ${body.string()}")
        goToVerifyScreen()
    }

    private fun goToVerifyScreen() {
        val intent = Intent(this, VerifyOtpActivity::class.java)
        intent.putExtra("mobile", mobileNo)
        startActivity(intent)
    }


    private fun handleError(t: Throwable?) {
        Log.d(tag, "handleError:  ${t?.localizedMessage}")
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }

}
