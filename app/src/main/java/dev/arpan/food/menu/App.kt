package dev.arpan.food.menu

import android.app.Application
import dev.arpan.food.menu.data.MenuRepository

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MenuRepository.init(this)
    }
}
