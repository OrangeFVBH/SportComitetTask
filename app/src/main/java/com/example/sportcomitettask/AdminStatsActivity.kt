
package com.example.sportcomitettask

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.util.* // Для использования Random

class AdminStatsActivity : AppCompatActivity() {

    // Определяем список базовых, заранее подобранных цветов
    // Эти цвета будут использоваться в первую очередь
    private val BASE_CHART_COLOR_RES_IDS = listOf(
        R.color.pie_chart_green_dark,
        R.color.pie_chart_yellow,
        R.color.pie_chart_red,
        R.color.pie_chart_blue,
        R.color.pie_chart_green_light,
        R.color.pie_chart_purple,
        R.color.pie_chart_orange,
        R.color.pie_chart_teal,
        R.color.pie_chart_pink,
        R.color.pie_chart_indigo,
        R.color.pie_chart_cyan,
        R.color.pie_chart_brown,
        R.color.pie_chart_grey_light,
        R.color.pie_chart_deep_orange,
        R.color.pie_chart_light_blue,
        R.color.pie_chart_lime,
        R.color.pie_chart_amber,
        R.color.pie_chart_deep_purple,
        R.color.pie_chart_light_green
        // Можете добавить еще ID цветов из colors.xml, если нужно
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_stats)

        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                // Предполагаем, что StatItem имеет поля value (Float) и label (String)
                val stats = RetrofitClient.api.getStats()

                if (stats.isNullOrEmpty()) {
                    Toast.makeText(this@AdminStatsActivity, "Статистика пока пуста", Toast.LENGTH_LONG).show()
                    return@launch
                }

                displayChart(stats)
            } catch (e: Exception) {
                Toast.makeText(this@AdminStatsActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun displayChart(stats: List<StatItem>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        pieChart.clear()

        val entries = stats.map { PieEntry(it.value, it.label) }

        val finalColors = ArrayList<Int>()

        // 1. Сначала добавляем все предопределенные цвета
        for (colorResId in BASE_CHART_COLOR_RES_IDS) {
            finalColors.add(ContextCompat.getColor(this, colorResId))
        }

        // 2. Если количество категорий больше, чем предопределенных цветов,
        // генерируем дополнительные уникальные цвета
        if (entries.size > finalColors.size) {
            val neededColorsCount = entries.size - finalColors.size
            // Шаг для изменения оттенка (Hue)
            // Мы делим 360 градусов на количество необходимых дополнительных цветов,
            // чтобы распределить их максимально равномерно по цветовому кругу.
            val hueStep = 360f / (neededColorsCount + finalColors.size) // Делим на общее количество сегментов

            // Начинаем генерировать с оттенка, который не сильно конфликтует с первыми цветами
            // Можно выбрать случайный начальный оттенок или фиксированный, например, 0f (красный)
            // Или продолжить от последнего использованного предопределенного цвета (более сложно)
            // Для простоты, начнем с небольшого смещения от 0
            var currentHue = 0f

            for (i in 0 until neededColorsCount) {
                // HSV (Hue, Saturation, Value) - более удобная модель для генерации
                // Hue (оттенок): от 0 до 360 (например, 0=красный, 120=зеленый, 240=синий)
                // Saturation (насыщенность): от 0 до 1 (0=серый, 1=полностью насыщенный)
                // Value (яркость): от 0 до 1 (0=черный, 1=полностью яркий)
                val hsv = floatArrayOf(currentHue, 0.7f, 0.7f) // Можно настроить Saturation и Value

                finalColors.add(Color.HSVToColor(hsv))
                currentHue = (currentHue + hueStep) % 360f // Переходим к следующему оттенку, циклично
            }
        }

        val dataSet = PieDataSet(entries, "Запросы").apply {
            colors = finalColors // Используем наш окончательный список цветов
            valueTextSize = 14f
            valueTextColor = Color.BLACK

            // Для соответствия скриншоту с "бубликовой" диаграммой
            // А также для размещения текста внутри или снаружи
            // sliceSpace = 2f // Промежутки между сегментами
            // valueLinePart1OffsetPercentage = 75f
            // valueLinePart1Length = 0.2f
            // valueLinePart2Length = 0.4f
            // valueLineColor = Color.BLACK // Цвет линий, ведущих к меткам
            // valueLineWidth = 1f // Ширина линии
            // xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Метки категории снаружи
            // yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Числовые значения снаружи
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false

        // Для "бубликовой" диаграммы, как на скриншоте:
        pieChart.isDrawHoleEnabled = true // Включить центральное отверстие
        pieChart.holeRadius = 58f       // Радиус отверстия (можно настроить)
        pieChart.setHoleColor(Color.TRANSPARENT) // Цвет отверстия (прозрачный или белый)
        pieChart.transparentCircleRadius = 61f // Радиус прозрачной окружности вокруг отверстия
        pieChart.setTransparentCircleColor(Color.WHITE) // Цвет прозрачной окружности
        pieChart.setTransparentCircleAlpha(100) // Прозрачность прозрачной окружности

        // Настройки для текста в центре, если нужно:
        // pieChart.setDrawCenterText(true)
        // pieChart.centerText = "Интересы"
        // pieChart.setCenterTextSize(16f)
        // pieChart.setCenterTextColor(Color.GRAY)


        pieChart.animateY(1000)
        pieChart.invalidate() // Перерисовать
    }
}