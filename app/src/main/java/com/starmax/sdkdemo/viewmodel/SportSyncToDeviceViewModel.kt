package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.Notify.LocationData
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class SportSyncToDeviceViewModel : ViewModel() , KoinComponent {
    var sportType by mutableStateOf(1)
    var sportStatus by mutableStateOf(1)
    var sportDistance by mutableStateOf(0)
    var speed by mutableStateOf(0)
    var locationData by mutableStateOf(listOf<LocationData>())
    var goalDistance by mutableStateOf(0)
    var goalHeat by mutableStateOf(0)
    var goalMinute by mutableStateOf(0)

    val context : Context by inject()

    fun randomLocationData(){
        locationData = generateContinuousCoordinates(1)
    }

    fun generateContinuousCoordinates(
        count: Int,
        maxStep: Double = 0.01
    ): List<LocationData> {
        require(count > 0) { "Count must be at least 1" }
        require(maxStep >= 0) { "Max step must be non-negative" }

        val random = Random.Default
        val coordinates = mutableListOf<LocationData>()

        // Generate initial coordinates
        var currentLng = random.nextDouble(-180.0, 180.0)
        var currentLat = random.nextDouble(-90.0, 90.0)
        coordinates.add(LocationData.newBuilder()
            .setLat(currentLat)
            .setLng(currentLng)
            .build())

        repeat(count - 1) {
            // Generate random offsets
            val offsetLng = random.nextDouble(-maxStep, maxStep)
            val offsetLat = random.nextDouble(-maxStep, maxStep)

            // Update current coordinates
            currentLng += offsetLng
            currentLat += offsetLat

            // Handling longitude boundaries (-180 to 180)
            currentLng = ((currentLng % 360) + 540) % 360 - 180

            // Handling latitude boundaries (-90 to 90)
            currentLat = currentLat.coerceIn(-90.0, 90.0)

            coordinates.add(LocationData.newBuilder()
                .setLat(currentLat)
                .setLng(currentLng)
                .build())
        }

        return coordinates.toList()
    }

    fun setData() {
        StarmaxBleClient.instance.sportSyncToDevice(
            sportType,
            sportStatus or 0x80,
            sportDistance,
            speed,
            locationData,
            goalDistance,
            goalHeat,
            goalMinute
        ).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "Sport data synced successfully", Toast.LENGTH_SHORT).show()
            }
        }, {
            viewModelScope.launch {
                Toast.makeText(context, "Failed to sync sport data", Toast.LENGTH_SHORT).show()
            }
        }).let { }
    }
}