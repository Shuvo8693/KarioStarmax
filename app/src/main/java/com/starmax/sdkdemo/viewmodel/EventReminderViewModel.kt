package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EventReminderViewModel : ViewModel(), KoinComponent {
    var eventReminders by mutableStateOf(listOf<com.starmax.bluetoothsdk.data.EventReminder>())
        private set
    var selectedIndex by mutableIntStateOf(0)
    val context: Context by inject()

    fun getData() {
        StarmaxBleClient.instance.getEventReminder().subscribe({
            eventReminders = it.eventRemindersList.map {
                com.starmax.bluetoothsdk.data.EventReminder(
                    year = it.year,
                    month = it.month,
                    day = it.day,
                    hour = it.hour,
                    minute = it.minute,
                    content = it.content,
                    remindType = it.remindType,
                    repeatType = it.repeatType,
                    repeats = it.repeatsList.toIntArray(),
                    otherInfo = it.otherInfo
                )
            }.toList()
        }, {

        }).let { }
    }

    fun addEventReminder() {
        val otherInfos = eventReminders.map {
            it.otherInfo
        }

        val canAddIds = (0..9).dropWhile {
            otherInfos.contains(it)
        }

        if (canAddIds.isEmpty()) {
            return
        }

        eventReminders = eventReminders.plus(
            com.starmax.bluetoothsdk.data.EventReminder(
                year = 0,
                month = 0,
                day = 0,
                hour = 0,
                minute = 0,
                content = "",
                remindType = 4,
                repeatType = 2,
                repeats = intArrayOf(),
                otherInfo = canAddIds[0]
            )
        ).toList()
    }

    fun removeEventReminder(index: Int) {
        eventReminders = eventReminders.filterIndexed { i, _ ->
            i != index
        }.toList()
    }

    fun refreshEventReminder(index: Int, newVal: com.starmax.bluetoothsdk.data.EventReminder) {
        eventReminders = eventReminders.mapIndexed { i, e ->
            if (i == index) {
                newVal.let {
                    com.starmax.bluetoothsdk.data.EventReminder(
                        year = it.year,
                        month = it.month,
                        day = it.day,
                        hour = it.hour,
                        minute = it.minute,
                        content = it.content,
                        remindType = it.remindType,
                        repeatType = it.repeatType,
                        repeats = it.repeats,
                        otherInfo = it.otherInfo
                    )
                }
            } else {
                e
            }
        }.toMutableList().toList()
    }

    fun setData() {
        StarmaxBleClient.instance.setEventReminder(eventReminders).subscribe({
            viewModelScope.launch {
                Toast.makeText(context, "设置事件提醒成功", Toast.LENGTH_SHORT).show()
            }
        }, {

        }).let { }
    }


}