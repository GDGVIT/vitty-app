package com.dscvit.vitty.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.util.VITMap

class NavigationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        if (intent.extras != null && intent.extras!!.getString("classId") != "") {
            startActivity(Intent(this, AuthActivity::class.java))
            VITMap.openClassMap(this, intent.extras!!.getString("classId")!!)
            finish()
        } else {
            Toast.makeText(this, "Class not found :(", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}