package com.starmax.sdkdemo

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.starmax.sdkdemo.ui.theme.AppTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


@Composable
fun PreviewInit(needStartKoin : Boolean = true,content: @Composable() () -> Unit){
    val context = LocalContext.current

    if(needStartKoin){
        startKoin {
            androidLogger()
            androidContext(context)
            modules(initModules())
        }
    }

    AppTheme() {
        CompositionLocalProvider(
            LocalNavController provides rememberNavController()
        ) {
            content()
        }
    }

}