package com.zkmsz.projemanage.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //setup ActionBar
        setupActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_sign_up_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) //let me back
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.back_button_signup)
        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed() //back when click on the backButton for actionbar
        }

    }

    //to register the user
    private fun registerUser()
    {
        //take the contains and delete the distances if any
        val name:String = et_name.text.toString().trim{ it <= ' '}
        val email:String = et_email.text.toString().trim{ it <= ' '}
        val password:String = et_password.text.toString().trim{ it <= ' '}

        //if all those value is entered
        if (validateForm(name,email,password))
        {
            //show the dialog for the user
            showProgressDialog(resources.getString(R.string.please_wait))

            //to add a new account
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email,password) //here set the email and password which is the user entered
                .addOnCompleteListener { task -> //when the register is completed

                    if(task.isSuccessful) //is the task is successful
                    {
                        val firebaseUser:FirebaseUser= task.result!!.user!! //make object to let me to access to details in the user For exam:email
                        val registeredEmail = firebaseUser.email!! //take the email

                        val user= User(firebaseUser.uid,name,registeredEmail) //make obj from my class User

                        //this to store the data for the user in the database
                        FirestoreClass().registerUser(this,user)
                    }

                    //if the register is failed
                    else {
                        Toast.makeText(this,task.exception!!.message, Toast.LENGTH_SHORT).show() //show the error in a toast
                    }


                }
        }
    }

    //sure if all the fields is not empty
    private fun validateForm(name: String, email:String, password:String):Boolean
    {
        return when
        {
            //if the name is empty
            TextUtils.isEmpty(name) ->
            {
                showErrorSnackBar("Please enter a name")
                false //return false
            }

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

    fun userRegisteredSuccess()
    {
        Toast.makeText(this,"you have successfully registered ",Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}
