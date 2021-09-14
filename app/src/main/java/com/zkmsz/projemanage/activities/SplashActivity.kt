package com.zkmsz.projemanage.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //to get all screen for splash
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //set a custom font to the text
        val typeFace= Typeface.createFromAsset(assets,"fontsplash.ttf")
        //tv_app_name.typeface = typeFace

        //to let me to move to another activity after 2 second
        Handler().postDelayed({
            var currentUserId= FirestoreClass().getCurrentUserId()

            if (currentUserId.isNotEmpty())
            {
                startActivity(Intent(this,
                    MainActivity::class.java))
            }
            else
            {
                startActivity(Intent(this,
                    IntroActivity::class.java))
            }

            finish()
        },1000)
    }
}
