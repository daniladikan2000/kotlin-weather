package com.example.weather

data class WeatherResponse (
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val sys: Sys,
    val name: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class Weather (
    val description: String,
    val icon: String
)

data class Wind (
    val speed: Double
)

data class Sys(
    val sunrise: Long,
    val sunset: Long
)