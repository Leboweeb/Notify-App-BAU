package com.example.notify

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
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
import java.util.Locale


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
    val listType = object : TypeToken<List<T>>() {}.type
    return listType
}

inline fun <reified T> gsonSerialze(json: String): List<T> {
    val type = getTokenTypeList<T>()
    return Gson().fromJson(json, type)
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

fun clearFragmentCache(fragment: Fragment, context: Context) {
    // application context is fine here
    var cacheKeyToClear = ""
//    if (fragment is HomeFragment) {}
    cacheKeyToClear = when (fragment) {
        is HomeFragment -> Services.XFILES.cacheKey
        is NotificationsFragment -> Services.NOTIFICATIONS.cacheKey
        is ScheduleFragment -> Services.SCHEDULE.cacheKey
        else -> ""
    }

    val prefs = getPrefs(context)
    prefs.edit().remove(cacheKeyToClear).apply()
}

fun showToastMessage(context: Context, message: String?) {
    val handler: Handler = Handler(Looper.getMainLooper())

    handler.post(Runnable { Toast.makeText(context, message, Toast.LENGTH_LONG).show() })
}

fun getResourceString(name: String, context: Context): String {
    val nameResourceID: Int =
        context.resources.getIdentifier(name, "string", context.applicationInfo.packageName)
    require(nameResourceID != 0) { "No resource string found with name $name" }
    return context.getString(nameResourceID)
}

fun postResponseToIconnect(context: Context, lifecycleOwner: LifecycleOwner, callback : (info : XFilesInfo) -> Unit ) {
    val localApi = LocalApi(context)
    localApi.postResponseToListener(
        forceNetwork = false,
        localApi::getXFilesInfo,   {
                info : String ->
            Gson().fromJson(info, XFilesInfo::class.java)
        }, "iconnect/xfiles"
    ).observe(lifecycleOwner) {
        info -> callback(info)
    }
}

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 * see https://stackoverflow.com/questions/67843986/is-there-a-shorter-replacement-for-kotlins-deprecated-string-capitalize-funct for more details
 */
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}