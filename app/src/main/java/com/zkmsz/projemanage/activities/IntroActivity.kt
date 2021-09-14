package com.zkmsz.projemanage.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.zkmsz.projemanage.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        //to full the screen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //set a custom font to the text
        val typeFace= Typeface.createFromAsset(assets,"fontsplash.ttf")
        tv_app_name_intro.typeface = typeFace
        tv_lets_go_intro.typeface = typeFace
        tv_des_intro.typeface = typeFace


        // go to SignUpActivity
        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //go to SignInActivity
        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }
}
