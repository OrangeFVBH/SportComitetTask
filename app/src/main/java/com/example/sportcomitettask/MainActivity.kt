package com.example.sportcomitettask

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sportsContainer: LinearLayout
    private lateinit var etCity: AutoCompleteTextView
    private lateinit var etSearch: EditText
    private val cities = arrayOf("Москва", "Стерлитамак", "Уфа", "Казань", "Санкт-Петербург")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sportsContainer = findViewById(R.id.sportsContainer)
        etCity = findViewById(R.id.etCity)
        etSearch = findViewById(R.id.etSearchSport)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        // Автозаполнение города [cite: 6, 7]
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        etCity.setAdapter(adapter)

        // Очистка названия города от лишних знаков при потере фокуса
        etCity.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cleanedText = etCity.text.toString().replace("\\s".toRegex(), "")
                etCity.setText(cleanedText)
            }
        }

        // Кнопка поиска по названию спорта
        btnSearch.setOnClickListener {
            val sport = etSearch.text.toString().trim()
            if (sport.isNotEmpty()) {
                openSectionsList(sport)
            } else {
                Toast.makeText(this, "Введите вид спорта", Toast.LENGTH_SHORT).show()
            }
        }

        // Поиск по нажатию кнопки "Enter" на клавиатуре
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btnSearch.performClick()
                true
            } else false
        }

        findViewById<Button>(R.id.btnGeo).setOnClickListener {
            etCity.setText("Стерлитамак")
            Toast.makeText(this, "Город выбран: Стерлитамак", Toast.LENGTH_SHORT).show()
        }

        loadSportsButtons()
    }

    private fun loadSportsButtons() {
        lifecycleScope.launch {
            try {
                val sports = RetrofitClient.api.getSports()
                sportsContainer.removeAllViews()
                for (sport in sports) {
                    val btn = Button(this@MainActivity)
                    btn.text = sport
                    btn.setOnClickListener { openSectionsList(sport) }
                    sportsContainer.addView(btn)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openSectionsList(sport: String) {
        val city = etCity.text.toString().trim()
        val intent = Intent(this, SectionsListActivity::class.java)
        intent.putExtra("SPORT", sport)
        intent.putExtra("CITY", city)
        startActivity(intent)
    }
}