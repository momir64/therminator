package rs.moma.therminator

import org.koin.android.ext.koin.androidContext
import rs.moma.therminator.di.androidModule
import rs.moma.therminator.di.sharedModule
import org.koin.core.context.startKoin
import android.app.Application

class TherminatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TherminatorApp)
            modules(sharedModule, androidModule)
        }
    }
}