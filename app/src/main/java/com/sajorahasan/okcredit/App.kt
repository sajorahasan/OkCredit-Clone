package com.sajorahasan.okcredit

import android.app.Application
import com.sajorahasan.okcredit.utils.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}