package com.example.sportcomitettask

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add)

        val btnSave = findViewById<Button>(R.id.btnSaveSection)

        btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {
        val data = mapOf(
            "org_name" to findViewById<EditText>(R.id.etAddOrgName).text.toString(),
            "leader" to findViewById<EditText>(R.id.etAddLeader).text.toString(),
            "address" to findViewById<EditText>(R.id.etAddAddress).text.toString(),
            "phone" to findViewById<EditText>(R.id.etAddPhone).text.toString(),
            "city" to findViewById<EditText>(R.id.etAddCity).text.toString(),
            "sports" to findViewById<EditText>(R.id.etAddSports).text.toString(),
            "schedule" to findViewById<EditText>(R.id.etAddSchedule).text.toString(),
            "age_groups" to findViewById<EditText>(R.id.etAddAgeGroups).text.toString()
        )

        if (data.values.any { it.isBlank() }) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.addSection(data)
                // Проверяем статус "added"
                if (response["status"] == "added") {
                    Toast.makeText(this@AdminAddActivity, "Организация добавлена!", Toast.LENGTH_SHORT).show()

                    // ВОЗВРАТ НАЗАД: закрываем текущую активити
                    finish()
                } else {
                    Toast.makeText(this@AdminAddActivity, "Ошибка: ${response["message"]}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAddActivity, "Сбой сети: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}