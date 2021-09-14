package com.zkmsz.projemanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.Board
import com.zkmsz.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.lang.Exception


class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFireUri: Uri? = null

    private lateinit var mUserName:String

    private var mBoardImageURL:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        //to get the name for the user from mainActivity
        if (intent.hasExtra(Constants.NAME))
        {
            mUserName= intent.getStringExtra(Constants.NAME)
        }

        iv_board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
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

        btn_create.setOnClickListener {
            if(mSelectedImageFireUri != null)
            {
                uploadBoardImage()
            }
            else
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }
    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_create_board_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_24dp)
        supportActionBar!!.title= resources.getString(R.string.create_board_title)
        toolbar_create_board_activity .setNavigationOnClickListener {
            onBackPressed()
        }
    }

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
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

        }
    }

    private fun createBoard()
    {
        val assignedUserArrayList:ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())

        var board= Board(et_board_name.text.toString(), mBoardImageURL, mUserName, assignedUserArrayList)

        FirestoreClass().createBoard(this,board)

    }

    //to upload the image to the firebase in the database
    private fun uploadBoardImage()
    {
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFireUri != null)
        {
            //here set a name of the image and his path
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "BOARD_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this,mSelectedImageFireUri))

            //here set the image
            sRef.putFile(mSelectedImageFireUri!!)
                .addOnSuccessListener {taskSnapshot -> //if successful

                    //to know where the image stored
                    Log.i("Board Image URI",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    //to take uri of the image to storage in the database
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                            uri ->
                        Log.i("Downloadable ImageURI", uri.toString())

                        //save the uri in this var
                        mBoardImageURL= uri.toString()

                        createBoard() //to automatically save the changes
                    }
                }.addOnFailureListener{exception ->

                    Toast.makeText(this,exception.message, Toast.LENGTH_SHORT).show()

                    hideProgressDialog()

                }
        }
    }

    //when the board created successfully
    fun boardCreatedSuccessfully()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
