package com.starmax.sdkdemo

import com.starmax.sdkdemo.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.createdAtStart

fun initModules() : List<Module>{
    return listOf(
        module{
            viewModelOf(::BleViewModel){
                createdAtStart()
            }
            viewModelOf(::OtaViewModel){
                createdAtStart()
            }
            viewModelOf(::HomeViewModel){
                createdAtStart()
            }
            viewModelOf(::SetNetModel){}
            viewModelOf(::SetStateViewModel){}
            viewModelOf(::CallControlViewModel){}
            viewModelOf(::CameraControlViewModel){}
            viewModelOf(::RealTimeDataOpenViewModel){}
            viewModelOf(::RealTimeMeasureOpenViewModel){}
            viewModelOf(::UserInfoViewModel){}
            viewModelOf(::GoalsViewModel){}
            viewModelOf(::HealthOpenViewModel){}
            viewModelOf(::HeartRateViewModel){}
            viewModelOf(::SetContactViewModel){}
            viewModelOf(::SetNotDisturbViewModel){}
            viewModelOf(::SetSosViewModel){}
            viewModelOf(::SetAppViewModel){}
            viewModelOf(::SetWorldClockViewModel){}
            viewModelOf(::PasswordViewModel){}
            viewModelOf(::FemaleHealthViewModel){}
            viewModelOf(::BloodSugarCalibrationViewModel){}
            viewModelOf(::BloodPressureCalibrationViewModel){}
            viewModelOf(::VolumeViewModel){}
            viewModelOf(::NfcCardViewModel){}
            viewModelOf(::CustomDeviceModeViewModel){}
            viewModelOf(::CustomDeviceNameViewModel){}
            viewModelOf(::CustomDeviceShakeTimeViewModel){}
            viewModelOf(::CustomDeviceShakeOnOffViewModel){}
            viewModelOf(::SportSyncToDeviceViewModel){}
            viewModelOf(::CustomSportModeOnOffViewModel){}
            viewModelOf(::CustomBroadcastViewModel){}
            viewModelOf(::DateFormatViewModel){}
            viewModelOf(::GoalsDayAndNightViewModel){}
            viewModelOf(::GoalsNotUpViewModel){}
            viewModelOf(::CustomHealthGoalsViewModel){}
            viewModelOf(::CustomHealthGoalTasksViewModel){}
            viewModelOf(::QuickBatteryModeViewModel){}
            viewModelOf(::FileSystemViewModel){}
            viewModelOf(::MessageViewModel){}
            viewModelOf(::Gts10HealthIntervalViewModel){}
            viewModelOf(::EventReminderViewModel){}
            viewModelOf(::Gts10PairViewModel){}
        }
    )
}