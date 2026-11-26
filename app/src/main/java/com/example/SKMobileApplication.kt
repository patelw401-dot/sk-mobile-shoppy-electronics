package com.example

import android.app.Application
import com.example.data.service.DataServiceLocator
import com.example.data.storage.LocalStorageManager

class SKMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalStorageManager.getInstance(applicationContext)
        DataServiceLocator.initialize(applicationContext)
    }
}
