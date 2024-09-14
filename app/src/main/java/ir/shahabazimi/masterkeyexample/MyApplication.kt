package ir.shahabazimi.masterkeyexample

import android.app.Application
import com.google.android.material.color.DynamicColors
import ir.shahabazimi.masterkeyexample.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}