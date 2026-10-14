package com.example.data.service

import android.content.Context
import com.example.data.firebase.FirebaseConfig

object DataServiceLocator {
    @Volatile
    private var dataServiceInstance: DataService? = null

    fun initialize(context: Context): DataService {
        return dataServiceInstance ?: synchronized(this) {
            dataServiceInstance ?: run {
                val service = if (FirebaseConfig.isConfigured(context)) {
                    FirestoreDataService.getInstance(context.applicationContext)
                } else {
                    LocalDataService.getInstance()
                }
                dataServiceInstance = service
                service
            }
        }
    }

    fun getDataService(): DataService {
        return dataServiceInstance ?: LocalDataService.getInstance()
    }

    fun setService(service: DataService) {
        dataServiceInstance = service
    }
}
