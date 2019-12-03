package com.sajorahasan.okcredit.activity.navigationActivity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.model.User
import com.sajorahasan.okcredit.room.KasbonRoomDb
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_statement.*
import java.text.SimpleDateFormat
import java.util.*


class StatementActivity : BaseActivity() {
    companion object {
        private const val TAG = "StatementActivity"
    }

    private lateinit var user: User
    private lateinit var db: KasbonRoomDb
    private var disposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statement)

        //Initialize views
        initViews()

        //Set data
        setData()
    }

    private fun initViews() {
        toolbar.setNavigationOnClickListener { onBackPressed() }

        disposable = CompositeDisposable()
        db = KasbonRoomDb.getDatabase(this)

        dateContainer.setOnClickListener { openCalender() }
    }

    private fun setData() {
        val calendar = Calendar.getInstance()
        val current = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val past = calendar.time
        val format = SimpleDateFormat("dd MMM yyyy")
        val date = format.format(past) + " - " + format.format(current)

        tvDateRange.text = date
    }

    @SuppressLint("SimpleDateFormat")
    private fun openCalender() {
        val cal = Calendar.getInstance()

        val dpd = DatePickerDialog(
            this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                tvDateRange.text = SimpleDateFormat("dd MMM yyyy").format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }


    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}
