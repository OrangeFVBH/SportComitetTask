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
        // Сбор данных из всех полей
        val data = mapOf(
            "org_name" to findViewById<EditText>(R.id.etAddOrgName).text.toString(),
            "leader" to findViewById<EditText>(R.id.etAddLeader).text.toString(),
            "address" to findViewById<EditText>(R.id.etAddAddress).text.toString(),
            "phone" to findViewById<EditText>(R.id.etAddPhone).text.toString(),
            "city" to findViewById<EditText>(R.id.etAddCity).text.toString(),
            "sports" to findViewById<EditText>(R.id.etAddSports).text.toString(),
            "schedule" to findViewById<EditText>(R.id.etAddSchedule).text.toString(),
            "age_groups" to findViewById<EditText>(R.id.etAddAgeGroups).text.toString() // Важно!
        )

        // Проверка на пустые поля
        if (data.values.any { it.isBlank() }) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Отправка на сервер
                val response = RetrofitClient.api.addSection(data)
                if (response["status"] == "added") {
                    Toast.makeText(this@AdminAddActivity, "Успешно добавлено!", Toast.LENGTH_SHORT).show()
                    finish() // Возвращаемся в меню админа
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAddActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}