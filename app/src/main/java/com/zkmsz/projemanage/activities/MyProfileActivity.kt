package com.zkmsz.projemanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.User
import com.zkmsz.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.lang.Exception

class MyProfileActivity : BaseActivity() {


    private var mSelectedImageFireUri:Uri?=null

    private var mProfileImageURL:String = ""

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this@MyProfileActivity)

        iv_profile_user_image.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
            else
            {
                //to request the permissions
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)

            }

        }

        btn_update.setOnClickListener {
            if(mSelectedImageFireUri != null)
            {
                uploadUserImage()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_my_profile_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_24dp)
        supportActionBar!!.title= resources.getString(R.string.my_profile)
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    //set the data in the user profile
    fun setUserDataInUI(user: User)
    {
        mUserDetails= user //set the details in the variable

        //set the image
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)

        if(user.mobile != 0L)
        {
            et_mobile.setText(user.mobile.toString())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
        }
        else
        {
            Toast.makeText(this,"Oops, you just denied the permission for storage",Toast.LENGTH_SHORT).show()
        }
    }


    //to check if we had an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //to set the image which the user is chose
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null)
        {
            mSelectedImageFireUri= data.data //the data now is the Uri for the image selected

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFireUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }

        }
    }

    //to update the data in the database
    private fun updateUserProfileData()
    {
        val userHashMap= HashMap<String,Any>()


        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image)
        {
            userHashMap[Constants.IMAGE]= mProfileImageURL //set the url of th image in the HashMap
        }

        if(et_name.text.toString() != mUserDetails.name)
        {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if(et_mobile.text.toString() != mUserDetails.mobile.toString())
        {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this,userHashMap) //to update the data in the database

    }

    //to upload the image
    private fun uploadUserImage()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        //First upload the image to the storage , after that to the database
        if(mSelectedImageFireUri != null)
        {
            //here set a name of the image and his path
            val sRef:StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this,mSelectedImageFireUri))

            //here set the image
            sRef.putFile(mSelectedImageFireUri!!)
                .addOnSuccessListener {taskSnapshot -> //if successful

                Log.i("Firebase Image URI",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    //to take uri of the image to storage in the data base
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                        Log.i("Downloadable ImageURI", uri.toString())

                        //save the uri in this var
                        mProfileImageURL= uri.toString()

                        updateUserProfileData() //to automatically save the changes
                    }
            }.addOnFailureListener{exception ->

                    Toast.makeText(this,exception.message,Toast.LENGTH_SHORT).show()

                    hideProgressDialog()

                }
        }
    }


    fun profileUpdateSuccess()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}
