package com.starmax.sdkdemo

import android.app.Application
import com.starmax.sdkdemo.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.createdAtStart

class MyApplication : Application() {

    override fun onCreate(){
        super.onCreate()

        System.loadLibrary("slm_m1_crack");

        startKoin {
            androidLogger(level = Level.INFO)
            androidContext(this@MyApplication)

            modules(initModules())
        }
    }
}