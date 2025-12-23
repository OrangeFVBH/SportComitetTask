package com.example.sportcomitettask

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity)

        findViewById<Button>(R.id.btnAddSection).setOnClickListener {
            startActivity(Intent(this, AdminAddActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewStats).setOnClickListener {
            startActivity(Intent(this, AdminStatsActivity::class.java))
        }
    }
}