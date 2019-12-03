package com.sajorahasan.okcredit.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.mancj.materialsearchbar.MaterialSearchBar
import com.san.app.activity.BaseActivity
import com.sajorahasan.okcredit.BuildConfig
import com.sajorahasan.okcredit.LanguageSelectionActivity
import com.sajorahasan.okcredit.R
import com.sajorahasan.okcredit.activity.customer.CustomerActivity
import com.sajorahasan.okcredit.activity.navigationActivity.AccountActivity
import com.sajorahasan.okcredit.activity.navigationActivity.ProfileActivity
import com.sajorahasan.okcredit.activity.navigationActivity.WebViewActivity
import com.sajorahasan.okcredit.adapter.ContactsPagerAdapter
import com.sajorahasan.okcredit.adapter.CustomerAdapter
import com.sajorahasan.okcredit.adapter.PhoneContactsAdapter
import com.sajorahasan.okcredit.model.Customer
import com.sajorahasan.okcredit.model.PhoneContact
import com.sajorahasan.okcredit.model.User
import com.sajorahasan.okcredit.room.KasbonRoomDb
import com.sajorahasan.okcredit.utils.Prefs
import com.sajorahasan.okcredit.utils.Tools
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*

@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
class HomeActivity : BaseActivity(), MaterialSearchBar.OnSearchActionListener, TextWatcher,
    View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    private val tag: String = "HomeActivity"
    private val REQUEST_READ_CONTACTS = 101

    private lateinit var pagerAdapter: ContactsPagerAdapter
    private lateinit var phoneContactLayout: LinearLayout
    private lateinit var phoneContactLoader: ProgressBar
    private lateinit var phoneContactList: MutableList<PhoneContact>
    private lateinit var phoneContactAdapter: PhoneContactsAdapter
    private lateinit var phoneContactRecyclerView: RecyclerView

    private lateinit var customerList: MutableList<Customer>
    private lateinit var customerAdapter: CustomerAdapter

    lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var db: KasbonRoomDb
    private var disposable: CompositeDisposable? = null

    private lateinit var user: User

    companion object {
        const val START_ACTIVITY_1_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Initialize views
        initViews()
    }

    private fun initViews() {
        customerList = mutableListOf()

        disposable = CompositeDisposable()
        db = KasbonRoomDb.getDatabase(this)

        tvVersionNo.text = getString(R.string.versionNo, BuildConfig.VERSION_NAME)

        etMaterialSearch.setSearchIcon(0)
        etMaterialSearch.setOnSearchActionListener(this)
        etMaterialSearch.addTextChangeListener(this)

        fbAdd.setOnClickListener(this)

        drawerSetup()

        initializeCustomerRecyclerView()

        // Initialize phone contact recyclerView
        phoneContactList = mutableListOf()
        initializePhoneContactRecyclerView()
        setupPhoneContactRecyclerView()
    }

    private fun drawerSetup() {
        mDrawerToggle = object : ActionBarDrawerToggle(
            this@HomeActivity,
            navDrawer,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {

            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                Log.e("NICK", "Sclose")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = Color.BLUE
                }
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = Color.GREEN
                }
                super.onDrawerOpened(drawerView)
                Log.e("NICK", "Sopen")
                //UpdateHeaderDetails()

                invalidateOptionsMenu()
            }
        }

        val headerView = navigationView.inflateHeaderView(R.layout.nav_header_main)
        val llHeaderMain = headerView.findViewById(R.id.llHeaderMain) as LinearLayout
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_account)

        llHeaderMain.setOnClickListener {
            goToProfile()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_account -> {
                gotoAccount()
            }
            R.id.nav_whatsapp -> {
                share()
            }
            R.id.nav_help -> {
                openWebView(getString(R.string.help), "https://www.google.com/")
            }
            R.id.nav_share -> {
                share()
            }
            R.id.nav_about_us -> {

            }
            R.id.nav_privacy_policy -> {
                openWebView(getString(R.string.privacy_policy), "https://www.google.com/")
            }
            R.id.nav_sign_out -> {
                showLogoutDialog()
            }
        }
        navDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun gotoAccount() {
        val intent = Intent(this, AccountActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (navDrawer.isDrawerOpen(GravityCompat.START)) {
            navDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openWebView(title: String, url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun goToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun share() {
        val shareBody = "Udhaar ka saara Hisaab apne Phone me - Install kabson for FREE"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(
            Intent.createChooser(sharingIntent, resources.getString(R.string.share_using))
        )
    }

    private fun initializeCustomerRecyclerView() {
        customerAdapter = CustomerAdapter(customerList, this)
        customerAdapter.setOnItemClickListener(object :
            CustomerAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val c = customerList[position]
                Log.d(tag, "onItemClick pos $position name $c")
                gotoCustomerScreen(view, c)
            }
        })
        rvCustomers.adapter = customerAdapter
    }

    @SuppressLint("InflateParams")
    private fun initializePhoneContactRecyclerView() {
        phoneContactLayout =
            layoutInflater.inflate(R.layout.layout_contact_list, null) as LinearLayout
        phoneContactLoader =
            phoneContactLayout.findViewById(R.id.phoneContactLoader)
        phoneContactLoader.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.colorPrimary),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )
        phoneContactRecyclerView =
            phoneContactLayout.findViewById(R.id.contactsRecyclerView)
        phoneContactRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.logout)
            .setMessage(R.string.logoutMsg)
            .setPositiveButton(R.string.logout) { dialog, _ ->
                Prefs.save("phone", "")
                Prefs.save("locale", "en")
                dialog.dismiss()
                gotoInitScreen()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create()
        builder.show()
    }

    private fun gotoInitScreen() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getUserFromDb(): User {
        val phone = Prefs.getString("phone")
        return db.userDao().getUser(phone!!)
    }

    private fun getUserData() {
        disposable!!.add(
            Single.create<User> { e -> e.onSuccess(getUserFromDb()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ getUserDataSuccess(it) }) { handleError(it) }
        )
    }

    private fun getUserDataSuccess(data: User) {
        Log.d(tag, "On Success: Got User Data Successfully! $data")
        user = data
        customerList.clear()
        customerList.addAll(user.customers)
        customerAdapter.notifyDataSetChanged()

        val headerView = navigationView.getHeaderView(0)
        val ivHeaderProfile = headerView.findViewById<ImageView>(R.id.imgProfile)
        val tvContactName = headerView.findViewById<TextView>(R.id.tvContactName)
        val tvContactNumber = headerView.findViewById<TextView>(R.id.tvContactNumber)

        if (user.profileImage.isNotEmpty()) {
            Glide.with(this)
                .load(user.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(ivHeaderProfile)
        }

        if (user.name.isNotEmpty()) {
            tvContactName.visibility = View.VISIBLE
            tvContactName.text = user.name
        }
        tvContactNumber.text = user.phone
    }

    private fun handleError(t: Throwable?) {
        Log.d(tag, "handleError:  ${t?.localizedMessage}")
    }

    private fun setupPhoneContactRecyclerView() {
        phoneContactAdapter = PhoneContactsAdapter(phoneContactList, this)
        phoneContactRecyclerView.adapter = phoneContactAdapter

        phoneContactAdapter.setOnItemClickListener(object :
            PhoneContactsAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                addCustomer(phoneContactList[position])
            }
        })
    }

    private fun addCustomerToDb(c: Customer): User {
        user.customers.add(c)
        db.userDao().updateUser(user)
        return getUserFromDb()
    }

    private fun addCustomer(c: PhoneContact) {
        Log.d(tag, "onItemClick PhoneContact--> $c")
        customerList.map {
            if (it.phone == c.phone) {
                Toast.makeText(this, "${c.phone}  is already added", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val t = Customer(c.name, c.phone, c.profileImage)

        disposable!!.add(
            Single.create<User> { e -> e.onSuccess(addCustomerToDb(t)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    user = it
                    customerList.clear()
                    customerList.addAll(user.customers)
                    customerAdapter.notifyDataSetChanged()
                    Log.d(tag, "Updated user is ==> $user")
                    etMaterialSearch.disableSearch()
                }) { handleError(it) }
        )
    }

    private fun gotoCustomerScreen(view: View, c: Customer) {
        Tools.hideKeyboard(view)
        val intent = Intent(this, CustomerActivity::class.java)
        intent.putExtra("user", user)
        intent.putExtra("customer", c)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fbAdd -> {
                if (!etMaterialSearch.isSearchEnabled) etMaterialSearch.enableSearch()
            }
        }
    }

    override fun onButtonClicked(buttonCode: Int) {
        Log.d(tag, "onButtonClicked $buttonCode")
        when (buttonCode) {
            MaterialSearchBar.BUTTON_BACK -> {
                etMaterialSearch.disableSearch()
            }
            MaterialSearchBar.BUTTON_NAVIGATION -> {
                navDrawer.openDrawer(Gravity.LEFT)
            }
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        Log.d(tag, "onSearchStateChanged $enabled")
        if (enabled) {
            //ivMenu.visibility = View.GONE
            //ivSort.visibility = View.GONE
            fbAdd.visibility = View.GONE
            rvCustomers.visibility = View.GONE
        } else {
            //ivMenu.visibility = View.VISIBLE
            //ivSort.visibility = View.VISIBLE
            fbAdd.visibility = View.VISIBLE
            rvCustomers.visibility = View.VISIBLE
        }
        setUpContactsUI()
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        Log.d(tag, "onSearchConfirmed ${text.toString()}")
    }

    override fun afterTextChanged(s: Editable?) {
        Log.d(tag, "afterTextChanged ${s.toString()}")
        if (s.toString().isNotEmpty()) {
            etMaterialSearch.setClearIcon(R.drawable.ic_close)
            val temp = phoneContactList.toMutableList()
            val res = temp.filter { it.name!!.contains(s.toString(), true) }
            Log.d(tag, "res----${res.toMutableList().size}")
            phoneContactAdapter = PhoneContactsAdapter(res.toMutableList(), this)
            phoneContactRecyclerView.adapter = phoneContactAdapter
            phoneContactAdapter.setOnItemClickListener(object :
                PhoneContactsAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    addCustomer(res[position])
                }
            })
            phoneContactAdapter.notifyDataSetChanged()
        } else {
            etMaterialSearch.setSearchIcon(0)
            setupPhoneContactRecyclerView()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_CONTACTS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(tag, "Permission has been denied by user")
                } else {
                    Log.i(tag, "Permission has been granted by user")
                    pagerAdapter = ContactsPagerAdapter()
                    viewPager.adapter = pagerAdapter
                    tabLayout.setupWithViewPager(viewPager)
                    pagerAdapter.addView(phoneContactLayout, 0)
                    pagerAdapter.notifyDataSetChanged()
                    if (phoneContactList.isNullOrEmpty()) loadContacts()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun setUpContactsUI() {
        pagerAdapter = ContactsPagerAdapter()
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        val inflater = layoutInflater

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            if (etMaterialSearch.isSearchEnabled) {
                val importContactLayout =
                    inflater.inflate(R.layout.item_import_contact, null) as LinearLayout
                val flImport = importContactLayout.findViewById<FrameLayout>(R.id.flImport)
                val tvImportTitle = importContactLayout.findViewById<TextView>(R.id.tvImportTitle)
                val tvImportDesc = importContactLayout.findViewById<TextView>(R.id.tvImportDesc)
                flImport.setOnClickListener { requestContactPermission() }
                tvImportTitle.setOnClickListener { requestContactPermission() }
                tvImportDesc.setOnClickListener { requestContactPermission() }
                pagerAdapter.addView(importContactLayout, 0)
                pagerAdapter.notifyDataSetChanged()
            }
        } else {
            if (etMaterialSearch.isSearchEnabled) {
                pagerAdapter.addView(phoneContactLayout, 0)
                pagerAdapter.notifyDataSetChanged()
            }
            if (phoneContactList.isNullOrEmpty()) loadContacts()
        }
    }

    private fun requestContactPermission() {
        Log.d(tag, "requestContactPermission")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            REQUEST_READ_CONTACTS
        )
    }

    private fun loadContacts() {
        disposable!!.add(
            Single.create<MutableList<PhoneContact>> { e -> e.onSuccess(getContacts()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoader() }
                .doFinally { hideLoader() }
                .subscribe({
                    if (it.size > 0) {
                        phoneContactList = mutableListOf()
                        phoneContactList.clear()
                        phoneContactList.addAll(it)
                        Log.d(tag, "loadContacts phoneContactList ${phoneContactList.size}")
                        setupPhoneContactRecyclerView()
                    } else {
                        Toast.makeText(
                            this,
                            "No contacts available to fetch from phone",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                }) { handleError(it) }
        )
    }

    private fun showLoader() {
        phoneContactRecyclerView.visibility = View.GONE
        phoneContactLoader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        phoneContactLoader.visibility = View.GONE
        phoneContactRecyclerView.visibility = View.VISIBLE
    }

    private fun getContacts(): MutableList<PhoneContact> {
        val tempList: MutableList<PhoneContact> = mutableListOf()
        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        if (cursor?.count!! > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                )).toInt()
                val photoURI =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        arrayOf(id),
                        null
                    )

                    if (cursorPhone?.count!! > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(
                                cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            tempList.add(PhoneContact(name, phoneNumValue, photoURI))
                        }
                    }
                    cursorPhone.close()
                }
            }
            return tempList.sortedBy { it.name }.toMutableList()
        } else {
            Log.d(tag, "No contacts")
            cursor.close()
            return tempList
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume called from Home")
        getUserData()
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop called from Home")
        disposable?.clear()
    }
}
