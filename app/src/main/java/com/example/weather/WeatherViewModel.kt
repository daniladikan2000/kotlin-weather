package com.example.weather

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val _weatherState = mutableStateOf<WeatherResponse?>(null)
    val weatherState: State<WeatherResponse?> = _weatherState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _cityInput = mutableStateOf("Moscow")
    val cityInput: State<String> = _cityInput

    private val _langCode = mutableStateOf("en")
    val langCode: State<String> = _langCode

    fun onCityInputChange (newCity: String) {
        _cityInput.value = newCity
    }

    fun fetchWeather() {
        if (cityInput.value.trim().isBlank()) {
            _error.value = "Error: The name of the city cannot be empty."
            return
        }


        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val cityName = cityInput.value.trim()
            val isCyrillic = cityName.any { it in 'а'..'я' || it in 'А'..'Я' }
            val determinedLangCode = if (isCyrillic) "ru" else "en"
            _langCode.value = determinedLangCode

            try {
                val response = RetrofitInstance.api.getWeather(
                    city = cityName,
                    lang = determinedLangCode
                )
                _weatherState.value = response
            } catch (e: Exception) {
                _error.value = "Error: The city was not found or there is no network."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
