package com.example.bettermoodle

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread

class JsonInterface(
    context: Context,
) {

    var preferences: EncryptedSharedPreferences
        private set
    var username: String
        private set
    var password: String
        private set

    init {
        this.preferences = getPrefs(context)!!
        this.username = preferences.getString("username", "")!!
        this.password = preferences.getString("password", "")!!
    }


    @JvmOverloads
    fun <T> postResponseToListener(
        forceNetwork: Boolean = false,
        // it is assumed T here is a type serializable to JSON
        callback: () -> T,
        deserializer: (String) -> T,
        // NOTE: cache key sample strings will be "moodle/notifications" or "iconnect/xfiles" similar to an HTTP endpoint for clarity.
        cacheKey: String,
    ): MutableLiveData<T> {
        val liveData = MutableLiveData<T>()
        if (getCachedJson(cacheKey) == "" || forceNetwork) {
            try {
                thread(start = true) {
                    val output = callback()
                    val serializedOutput = Gson().toJson(output)
                    liveData.postValue(output)
                    serializeToPreferencesIfNotFound(
                        preferences, cacheKey, serializedOutput, true
                    )
                }.join()
            } catch (e: ExecutionException) {
                logStackTrace("HTTP Tag", e)
            } catch (e: InterruptedException) {
                logStackTrace("HTTP Tag", e)
            }

        } else {
            // if we already have a response, just return from cache
            // THIS CLASS IS NOT RESPONSIBLE FOR REFRESHING DATA
            // MAKE SURE ANOTHER CLASS DOES
            val cachedJson = getCachedJson(cacheKey)
            liveData.postValue(deserializer(cachedJson))
        }
        return liveData
    }


    private fun getCachedJson(cacheKey: String): String {
        return preferences.getString(cacheKey, "")!!
    }


//    private fun getCachedJsonTimestamp(): Duration? {
//        val expires: Long = JSONObject(getCachedJson()).getLong("expires")
//        val expiresDateTime =
//            LocalDateTime.ofInstant(Instant.ofEpochMilli(expires), ZoneId.systemDefault())
//        val now = LocalDateTime.now()
//        return Duration.between(now, expiresDateTime)
//    }

//    fun moodleInvalidator(): Boolean {
//        return getCachedJsonTimestamp()!!.toHours() >= 8
//    }

}