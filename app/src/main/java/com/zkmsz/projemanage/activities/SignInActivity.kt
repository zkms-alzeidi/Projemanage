package com.zkmsz.projemanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.et_email
import kotlinx.android.synthetic.main.activity_sign_in.et_password

class SignInActivity : BaseActivity() {


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setupActionBar()

        //initial auth
        auth= FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_sign_in_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) //let me back
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.back_button_signup)
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed() //back when click on the backButton for actionbar
        }
    }

    private fun signInRegisteredUser()
    {

        //take the contains and delete the distances if any
        val email:String = et_email.text.toString().trim{ it <= ' '}
        val password:String = et_password.text.toString().trim{ it <= ' '}

        //check if all is not empty
        if(validateForm(email,password))
        {
            showProgressDialog(resources.getString(R.string.please_wait))

            //check if user signIn
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {task ->

                    //check is the task is successful
                    if (task.isSuccessful)
                    {

                        Log.d("Sign in","Successful")
                        val user= auth.currentUser //take the id for the current user

                        FirestoreClass().loadUserData(this) //take the data for the current user

                    }
                    //if the task is failed
                    else
                    {
                        Log.w("Sign in", "Failure",task.exception)
                        Toast.makeText(this,"Authentication failed",Toast.LENGTH_LONG).show()
                    }
                }



        }



    }

    //sure if all the fields is not empty
    private fun validateForm(email:String, password:String):Boolean
    {
        return when
        {
            //if the email is empty
            TextUtils.isEmpty(email) ->
            {
                showErrorSnackBar("Please enter an email address")
                false //return false
            }

            //if the password is empty
            TextUtils.isEmpty(password) ->
            {
                showErrorSnackBar("Please enter a password")
                false //return false
            }

            else ->
            {
                return true
            }
        }
    }

    fun signInSuccess(user: User?)
    {
        startActivity(Intent(this,MainActivity::class.java))//go to main activity
        finish()
        hideProgressDialog()
    }

}
