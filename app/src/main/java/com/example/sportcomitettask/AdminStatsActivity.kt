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
                if (stats.isEmpty()) {
                    Toast.makeText(this@AdminStatsActivity, "Нет данных для статистики", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                displayChart(stats)
            } catch (e: Exception) {
                Toast.makeText(this@AdminStatsActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayChart(stats: List<StatItem>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // Превращаем данные от сервера в формат диаграммы
        val entries = stats.map{ PieEntry(it.value, it.label) }

        val dataSet = PieDataSet(entries, "Виды спорта")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Популярность"
        pieChart.animateY(1000) // Анимация отрисовки
        pieChart.invalidate()
    }
}