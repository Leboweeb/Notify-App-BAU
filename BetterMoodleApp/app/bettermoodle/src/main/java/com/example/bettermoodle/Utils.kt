package com.example.bettermoodle

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.security.GeneralSecurityException


enum class HTTPMethod {
    GET, POST;

}


@Suppress("SameParameterValue")
fun logStackTrace(tag: String, e: Exception) {
    Log.e(tag, e.cause.toString())
}

fun clearPrefs(context: Context) {
    val prefs = getPrefs(context)
    prefs.edit().clear().apply()
}

fun getPrefs(context: Context): EncryptedSharedPreferences {
    var masterKeyAlias: String? = null
    try {
        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    } catch (e: Exception) {
        logStackTrace("Security Tag", e)
    }
    var sharedPreferences: EncryptedSharedPreferences? = null
    try {
        sharedPreferences =
            EncryptedSharedPreferences.create( // passing a file name to share a preferences
                "preferences",
                masterKeyAlias!!,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
    } catch (e: GeneralSecurityException) {
        logStackTrace("Security Tag", e)
    } catch (e: IOException) {
        logStackTrace("Security Tag", e)
    }
    return sharedPreferences!!
}

@JvmOverloads
fun serializeToPreferencesIfNotFound(
    preferences: EncryptedSharedPreferences,
    key: String,
    value: String,
    overwrite: Boolean = false
) {
    if (!preferences.contains(key) || overwrite)
        preferences.edit().putString(key, value)
            .apply()
}

private fun Request.Builder.request(
    method: HTTPMethod = HTTPMethod.GET,
    body: String? = null
): Request.Builder {
    return if (method == HTTPMethod.GET) {
        this
    } else {
        val jsonBody = body!!.toRequestBody("application/json".toMediaType())
        this.post(jsonBody)
    }
}


fun JSONArray.toList(): ArrayList<Any> {
    val tempList = arrayListOf<Any>();
    for (i in 0..<this.length()) {
        tempList.add(this[i])
    }

    return tempList
}

fun bitmapToB64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return Base64.encodeToString(
        outputStream.toByteArray(),
        Base64.DEFAULT
    )!!
}

fun b64ToBitmap(encodedString: String): Bitmap {
    val decodedString: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)!!

}

inline fun <reified T> getTokenTypeList(): Type {
    val listType = object : TypeToken<ArrayList<T>>() {}.type
    return listType
}

inline fun <reified T> deserializetoListType(string: String): T {
    val listType = object : TypeToken<ArrayList<T>>() {}.type
    return Gson().fromJson(string, listType)
}

fun getActivity(context: Context?): Activity? {
    if (context == null) {
        return null
    } else if (context is ContextWrapper) {
        return if (context is Activity) {
            context
        } else {
            getActivity(context.baseContext)
        }
    }

    return null
}