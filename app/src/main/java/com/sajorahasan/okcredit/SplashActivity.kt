package com.sajorahasan.okcredit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.activity.HomeActivity
import com.sajorahasan.okcredit.utils.Prefs
import com.sajorahasan.okcredit.utils.Utils

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Check if user is logged in or not
        checkLogin()
    }

    private fun checkLogin() {
        val phone = Prefs.getString("phone")
        Log.d("SplashActivity", "SignIn status phone is => $phone")
        if (phone.isNullOrEmpty()) gotoWelcome()
        else gotoHome()
    }

    private fun gotoHome() {
        Utils.setLocale(this, Prefs.getString("locale"))

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun gotoWelcome() {
        Utils.setLocale(this, "en")
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }


}
