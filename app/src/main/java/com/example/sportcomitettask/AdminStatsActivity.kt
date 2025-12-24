package com.example.sportcomitettask

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

class AdminStatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_stats)

        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val stats = RetrofitClient.api.getStats()

                // ПРОВЕРКА: Если данных нет, не вызываем displayChart
                if (stats.isNullOrEmpty()) {
                    Toast.makeText(this@AdminStatsActivity, "Статистика пока пуста", Toast.LENGTH_LONG).show()
                    // Можно вывести текст "Нет данных" на экран вместо диаграммы
                    return@launch
                }

                displayChart(stats)
            } catch (e: Exception) {
                // Если сервер выдал ошибку, приложение не вылетит, а покажет Toast
                Toast.makeText(this@AdminStatsActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun displayChart(stats: List<StatItem>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // Очищаем старые данные перед отрисовкой новых
        pieChart.clear()

        val entries = stats.map { PieEntry(it.value, it.label) }

        val dataSet = PieDataSet(entries, "Запросы").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate() // Перерисовать
    }
}