package com.zkmsz.projemanage.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.JsonToken
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.adapters.BoardItemsAdapter
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.Board
import com.zkmsz.projemanage.models.User
import com.zkmsz.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    companion object
    {
        const val MY_PROFILE_REQUEST_CODE:Int= 11
        const val CREATE_BOARD_REQUEST_CODE:Int=12
    }

    private lateinit var mUserName:String

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()


        //to let me clicked on the items which are in the navigation
        nav_view.setNavigationItemSelectedListener(this)

        mSharedPreferences= this.getSharedPreferences(Constants.PRJEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated= mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated)
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this,true)
        }
        else
        {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity)
            {instansceIdResult->
                updateFCMToken(instansceIdResult.token)
            }
        }

        FirestoreClass().loadUserData(this, true) //set true because we need to load the list when the user inter for the first time

        //when click on the floatActionButton
        fab_create_board.setOnClickListener {
            val intent= Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName) //to send the user name to the CreatActivity
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }



    }

    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_main_activity)
        //add icon
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        //when click on the icon on the actionbar
        toolbar_main_activity.setNavigationOnClickListener {
            //Toggle drawer
            toggleDrawer()// open and close the navigation
        }

    }

    //this fun to open and close the navigation
    private fun toggleDrawer()
    {
        if(drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {

        if(drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            doubleBackToExit() //exit from the app
        }
    }

    //this fun let me to click on the item in the navigation
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.nav_my_profile ->
            {
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE) //go to MyProfileActivity
            }

            R.id.nav_sign_out ->
            {
                FirebaseAuth.getInstance().signOut() //to signOut from the account

                mSharedPreferences.edit().clear().apply()

                //to open IntroActivity
                val intent=Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //to update the data in the navigation
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean)
    {
        hideProgressDialog()
        mUserName= user.name //to send hte user name to the Board to let me knew who is created
        //to set the user image
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        //to set name of user
        tv_username.text= user.name

        if (readBoardsList)
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE)
        {
            FirestoreClass().loadUserData(this)

        }
        else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE)
        {
            FirestoreClass().getBoardsList(this)

        }
        else
        {
            Log.e("Canceled","Canceled")
        }
    }

    //take tha data for the board and display
    fun populateBoardsListToUI(boardsList:ArrayList<Board>)
    {
        hideProgressDialog()
        if(boardsList.size > 0)
        {
            rv_boards_list.visibility= View.VISIBLE
            tv_no_boards_available.visibility= View.GONE

            rv_boards_list.layoutManager= LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter=BoardItemsAdapter(this,boardsList)
            rv_boards_list.adapter= adapter

            //when click on one of the item in the recyclerView
            adapter.setOnClickListener(object :BoardItemsAdapter.OnClickListener
            {
                override fun onClick(position: Int, model: Board) {
                    val intent= Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent) //go to taskActivityList
                }
            })


        }
        else
        {
            rv_boards_list.visibility= View.GONE
            tv_no_boards_available.visibility= View.VISIBLE
        }
    }

    fun tokenUpdateSuccess()
    {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this,true)
    }

    private fun updateFCMToken(token:String)
    {
        val userHashMap= HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}
