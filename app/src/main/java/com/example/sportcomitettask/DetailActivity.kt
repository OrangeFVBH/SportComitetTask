package com.example.sportcomitettask

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val org = intent.getStringExtra("ORG")
        val coach = intent.getStringExtra("COACH")
        val phone = intent.getStringExtra("PHONE")
        val address = intent.getStringExtra("ADDRESS")
        val schedule = intent.getStringExtra("SCHEDULE")

        findViewById<TextView>(R.id.detailOrg).text = org
        findViewById<TextView>(R.id.detailCoach).text = "Тренер/Руководитель:\n$coach"
        findViewById<TextView>(R.id.detailAddress).text = "Адрес: $address\n(Нажмите чтобы открыть карту)"
        findViewById<TextView>(R.id.detailSchedule).text = "Расписание: $schedule"

        // 1. Открытие карт (Бесплатный API через Intent)
        findViewById<TextView>(R.id.detailAddress).setOnClickListener {
            // Создаем URI для поиска по адресу
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            // Пытаемся запустить приложение карт
            startActivity(mapIntent)
        }

        // 2. Кнопка звонка с подтверждением
        findViewById<Button>(R.id.btnCall).setOnClickListener {
            showCallConfirmation(phone)
        }
    }

    private fun showCallConfirmation(phone: String?) {
        if (phone.isNullOrEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Позвонить?")
            .setMessage("Вы точно хотите набрать номер: $phone?")
            .setPositiveButton("Да") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                startActivity(intent)
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}