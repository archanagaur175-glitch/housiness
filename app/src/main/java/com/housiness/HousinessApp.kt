package com.housiness

import android.app.Application
import com.housiness.di.ServiceLocator

class HousinessApp : Application() {

    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(this)
    }
}
