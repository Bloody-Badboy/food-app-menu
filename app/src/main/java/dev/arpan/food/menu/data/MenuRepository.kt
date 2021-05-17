package dev.arpan.food.menu.data

import android.app.Application
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.Buffer

class MenuRepository private constructor(private val application: Application) {
    companion object {
        private var INSTANCE: MenuRepository? = null
        fun init(application: Application) {
            INSTANCE = MenuRepository(application = application)
        }

        fun getInstance() = checkNotNull(INSTANCE) {
            "Repository not initialized"
        }
    }

    private val moshi = Moshi.Builder()
        .add(YesNoBooleanAdapter())
        .build()
    private val adapter = moshi.adapter<List<MenuCategory>>(
        Types.newParameterizedType(
            List::class.java,
            MenuCategory::class.java
        )
    )

    suspend fun getMenuMenuCategory(): List<MenuCategory> {
        return withContext(Dispatchers.IO) {
            delay(2500L)
            runCatching {
                val buffer = Buffer().readFrom(application.assets.open("menu.json"))
                val reader = JsonReader.of(buffer)
                checkNotNull(adapter.fromJson(reader))
            }
        }.getOrThrow()
    }

}