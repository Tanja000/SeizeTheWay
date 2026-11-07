package com.example.seizetheway

import android.app.Application
import com.example.seizetheway.data.SavedTripsStorage

class SeizeTheWayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SavedTripsStorage.init(this)
    }
}
