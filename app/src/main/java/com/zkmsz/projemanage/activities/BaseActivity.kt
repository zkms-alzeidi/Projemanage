package com.zkmsz.projemanage.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.TokenWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zkmsz.projemanage.R
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text:String)
    {
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.tv_progress_text.text= text
        mProgressDialog.show()
    }

    fun hideProgressDialog()
    {
        mProgressDialog.dismiss()
    }

    fun  getCurrentUserId():String
    {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    //if click twins on the back button close the activity
    fun doubleBackToExit(){

        if (doubleBackToExitPressedOnce) //if it is true
        {
            super.onBackPressed() //back
            return
        }

        this.doubleBackToExitPressedOnce= true

        //make Toast
        Toast.makeText(
            this,resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_LONG
        ).show()

        //handler 2 seconds
        Handler().postDelayed({
            doubleBackToExitPressedOnce =false
        }, 2000)
    }

    fun showErrorSnackBar(message:String)
    {
        val snackBar= Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView= snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_error_color))

        snackBar.show()
    }
}
