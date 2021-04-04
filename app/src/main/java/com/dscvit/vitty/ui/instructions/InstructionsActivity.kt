package com.dscvit.vitty.ui.instructions

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.ui.schedule.ScheduleActivity

class InstructionsActivity : AppCompatActivity() {

    private lateinit var doneButton: Button
    private lateinit var greetingsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        val message = intent.getStringExtra(EXTRA_MESSAGE)

        doneButton = findViewById(R.id.done_button)
        greetingsTextView = findViewById(R.id.greeting)

        "Hello, $message".also { greetingsTextView.text = it }

        doneButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}