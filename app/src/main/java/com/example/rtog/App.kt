package com.example.rtog

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("e5ff1a16-9199-4803-a756-914f69c6d6b5") // замени на свой ключ
        MapKitFactory.initialize(this)
    }
}