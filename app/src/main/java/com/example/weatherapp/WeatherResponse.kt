package com.example.weatherapp

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val sys: Sys,
    val dt: Long
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
)

data class Sys(
    val sunrise: Long,
    val sunset: Long
)

data class Weather(
    val main: String
)