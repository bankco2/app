package kr.roo.team1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(Runnable {
            val i = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }, 2000)
    }
}
