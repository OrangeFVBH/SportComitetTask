package com.example.sportcomitettask

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SectionsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sections_list)

        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val sport = intent.getStringExtra("SPORT") ?: ""
        val city = intent.getStringExtra("CITY") ?: ""

        title = "Результаты: $sport"

        lifecycleScope.launch {
            try {
                val sections = RetrofitClient.api.searchSections(city, sport)
                listContainer.removeAllViews()

                if (sections.isEmpty()) {
                    Toast.makeText(this@SectionsListActivity, "Ничего не найдено", Toast.LENGTH_SHORT).show()
                }

                for (section in sections) {
                    val card = layoutInflater.inflate(R.layout.item_section, null)
                    card.findViewById<TextView>(R.id.tvOrgName).text = section.org_name
                    card.findViewById<TextView>(R.id.tvAddress).text = section.address

                    // Наполняем теги (блоки) видов спорта
                    val tagsContainer = card.findViewById<LinearLayout>(R.id.tagsContainer)
                    for (s in section.sports) {
                        val tag = TextView(this@SectionsListActivity)
                        tag.text = s
                        tag.setPadding(20, 10, 20, 10)
                        tag.setBackgroundResource(android.R.drawable.btn_default) // Можно заменить на свою форму
                        val params = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 15, 0)
                        tag.layoutParams = params
                        tagsContainer.addView(tag)
                    }

                    card.findViewById<Button>(R.id.btnDetails).setOnClickListener {
                        val intent = Intent(this@SectionsListActivity, DetailActivity::class.java)
                        intent.putExtra("ORG", section.org_name)
                        intent.putExtra("COACH", section.leader)
                        intent.putExtra("PHONE", section.phone)
                        intent.putExtra("ADDRESS", section.address)
                        intent.putExtra("SCHEDULE", section.schedule)
                        startActivity(intent)
                    }
                    listContainer.addView(card)
                }
            } catch (e: Exception) {
                Toast.makeText(this@SectionsListActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }
}