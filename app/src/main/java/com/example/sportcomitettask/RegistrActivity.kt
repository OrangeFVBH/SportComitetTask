package com.example.sportcomitettask

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etLogin = findViewById<EditText>(R.id.etRegLogin)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val data = mapOf("login" to login, "password" to password)
                    val response = RetrofitClient.api.register(data)

                    if (response["status"] == "success") {
                        Toast.makeText(this@RegisterActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                        finish() // Возврат на экран логина
                    }
                } catch (e: Exception) {
                    // Обработка ошибки 409 (пользователь существует)
                    Toast.makeText(this@RegisterActivity, "Ошибка: имя занято или сервер недоступен", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}