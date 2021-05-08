package dev.arpan.food.menu.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class YesNoBoolean

class YesNoBooleanAdapter {
    @ToJson
    fun toJson(@YesNoBoolean value: Boolean): String {
        return if (value) "Yes" else "No"
    }

    @FromJson
    @YesNoBoolean
    fun fromJson(string: String): Boolean {
        return string.equals("Yes", true)
    }
}
