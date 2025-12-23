package com.example.sportcomitettask

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Отправляем запрос на сервер
            loginToServer(login, password)
        }
        findViewById<Button>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginToServer(login: String, password: String) {
        lifecycleScope.launch {
            try {
                val credentials = mapOf("login" to login, "password" to password)
                val response = RetrofitClient.api.login(credentials)

                if (response.status == "success") {
                    if (response.role == "admin") {
                        // Переход на экран админа (нужно будет создать AdminActivity)
                        Toast.makeText(this@LoginActivity, "Вход как Админ", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Переход на главный экран поиска для пользователя
                        Toast.makeText(this@LoginActivity, "Вход как Пользователь", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    finish() // Закрываем экран логина
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
            }
        }
    }
}