package com.example.weatherapp

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.ImageView
import android.widget.FrameLayout
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var api: WeatherApi
    private lateinit var pulseAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(WeatherApi::class.java)

        // UI bindings
        val tempText = findViewById<TextView>(R.id.tempText)
        val conditionText = findViewById<TextView>(R.id.conditionText)
        val cityInput = findViewById<EditText>(R.id.cityInput)
        val humidityText = findViewById<TextView>(R.id.humidityText)
        val windText = findViewById<TextView>(R.id.windText)
        val feelsLikeText = findViewById<TextView>(R.id.feelsLikeText)
        val cityNameText = findViewById<TextView>(R.id.cityNameText)
        val weatherCircle = findViewById<LinearLayout>(R.id.weatherCircle)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val rootLayout = findViewById<FrameLayout>(R.id.rootLayout)
        val sunIcon = findViewById<ImageView>(R.id.sunIcon)

        // Animations
        val circleAnim = AnimationUtils.loadAnimation(this, R.anim.fade_scale)
        pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)

        weatherCircle.startAnimation(circleAnim)
        sunIcon.startAnimation(pulseAnim)

        rootLayout.setBackgroundResource(R.drawable.sunny_bg)

        // Default city on launch
        fetchWeather(
            "Pune",
            cityNameText,
            tempText,
            conditionText,
            humidityText,
            windText,
            feelsLikeText,
            rootLayout,
            sunIcon
        )

        // Search button
        searchButton.setOnClickListener {

            val city = cityInput.text.toString().trim()

            if (city.isEmpty()) {
                Toast.makeText(this, "Enter city", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchWeather(
                city,
                cityNameText,
                tempText,
                conditionText,
                humidityText,
                windText,
                feelsLikeText,
                rootLayout,
                sunIcon
            )
        }
    }

    private fun fetchWeather(
        city: String,
        cityNameText: TextView,
        tempText: TextView,
        conditionText: TextView,
        humidityText: TextView,
        windText: TextView,
        feelsLikeText: TextView,
        rootLayout: FrameLayout,
        sunIcon: ImageView
    ) {

        val call = api.getWeather(city, BuildConfig.API_KEY)

        call.enqueue(object : Callback<WeatherResponse> {

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {

                if (response.isSuccessful && response.body() != null) {

                    val data = response.body()!!
                    val weatherType = data.weather[0].main

                    // Update UI
                    tempText.text = "${data.main.temp.toInt()}°"
                    conditionText.text = weatherType
                    humidityText.text = "${data.main.humidity}%"
                    windText.text = "${data.wind.speed} km/h"
                    feelsLikeText.text = "${data.main.feels_like.toInt()}°"
                    cityNameText.text = data.name

                    // Night detection
                    val isNight = data.dt < data.sys.sunrise || data.dt > data.sys.sunset

                    // Restart animation
                    sunIcon.clearAnimation()
                    sunIcon.startAnimation(pulseAnim)

                    if (isNight) {
                        sunIcon.setImageResource(R.drawable.ic_night)
                        rootLayout.setBackgroundResource(R.drawable.night_bg)
                    } else {
                        when (weatherType) {
                            "Clear" -> {
                                sunIcon.setImageResource(R.drawable.ic_sunny)
                                rootLayout.setBackgroundResource(R.drawable.sunny_bg)
                            }
                            "Clouds" -> {
                                sunIcon.setImageResource(R.drawable.ic_cloud)
                                rootLayout.setBackgroundResource(R.drawable.cloudy_bg)
                            }
                            "Rain", "Drizzle", "Thunderstorm" -> {
                                sunIcon.setImageResource(R.drawable.ic_rain)
                                rootLayout.setBackgroundResource(R.drawable.rainy_bg)
                            }
                            else -> {
                                sunIcon.setImageResource(R.drawable.ic_sunny)
                            }
                        }
                    }

                } else {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}