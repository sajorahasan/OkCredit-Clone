package com.sajorahasan.okcredit

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.utils.Prefs
import com.sajorahasan.okcredit.utils.Utils
import kotlinx.android.synthetic.main.activity_language_selection.*

class LanguageSelectionActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        //Initialize views
        initViews()
    }

    private fun initViews() {
        cvEng.setOnClickListener(this)
        cvInd.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cvEng -> gotoWelcomeScreen("en")
            R.id.cvInd -> gotoWelcomeScreen("in")
        }
    }

    private fun gotoWelcomeScreen(localeName: String) {
        Prefs.save("locale", localeName)
        Utils.setLocale(this, localeName)

//        val phone = Prefs.getString("phone")
//        if (phone!!.isNotBlank()) {
//            finish()
//        } else {
        startActivity(Intent(this, WelcomeActivity::class.java))
//        }
    }
}
