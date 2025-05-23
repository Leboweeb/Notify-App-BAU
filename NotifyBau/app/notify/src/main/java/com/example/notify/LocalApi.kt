package com.example.notify


import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.example.JavaNetCookieJar
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class LocalApi(
    private val context: Context,
) {

    private val userAgent = RandomUserAgent.getRandomUserAgent()
    private val authURL = "https://eis.bau.edu.lb/commonauth"
    private val client = httpClientBuilder()
    private val preferences =
        getPrefs(context)
    private val username = preferences.getString("username", "") ?: ""
    private val password = preferences.getString("password", "") ?: ""


    private fun login(service: BauService): Response? {
        val response = performRequest(service.url).get()
        val responseBody = response.body?.string() ?: throw Exception("HTML body not found!")
        val doc = Jsoup.parse(responseBody)
        val sessionDataKey = doc.select("[name=sessionDataKey]").attr("value")
        println("session key : $sessionDataKey")
        val loginPayload = mapOf(
            "usernameUserInput" to this.username,
            "username" to "${this.username}@carbon.super",
            "password" to this.password,
            "sessionDataKey" to sessionDataKey
        )
        val formBody = FormBody.Builder()
        for ((key, value) in loginPayload.entries) {
            formBody.addEncoded(key, value)
        }
        val requestBody = formBody.build()
        val loginRequest = Request.Builder().url(authURL).removeHeader("User-Agent")
            .addHeader("User-Agent", this.userAgent).addHeader(
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            ).addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Connection", "keep-alive").post(requestBody)
        val loginResponse = performRequest(loginRequest.build())
        return loginResponse.get()
    }

    private fun connectToMoodle(): Triple<String, String, String> {
        val responseBody = login(BauService.MOODLE).getBody()
        val document = Jsoup.parse(responseBody)
        val sessionkey = document.select("[name=sesskey]").attr("value")
        val userid = document.select("#nav-notification-popover-container").attr("data-userid")
        return Triple(responseBody, sessionkey, userid)

    }

    private fun connectToIconnect(): String {
        var loginResponse = login(BauService.ICONNECT)
        val url = loginResponse!!.networkResponse!!.request.url
        if (url != BauService.ICONNECT.finalUrl) {
            // we got redirected yet again, perform get request
            loginResponse = this.performRequest(BauService.ICONNECT.finalUrl.toString()).get()
        }
        val finalResponseBody = loginResponse.getBody()
        return finalResponseBody

    }

    //    @Throws(FailedToScrapeException::class)
    fun getXFilesInfo(): XFilesInfo {
        connectToIconnect()
        val xfilesRequest = Request.Builder()
            .url("https://mis.bau.edu.lb/web/v12/iconnectv12/cas/intermediate.aspx?TargetURL=https://mis.bau.edu.lb/web/v12/iconnectv12/base/profileV2.aspx")
            .addHeader("Accept-Language", "en-US,en;q=0.7")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br, zstd").addHeader("Priority", "u=0, i")
            .addHeader("Sec-Fetch-Dest", "document").addHeader("Sec-Fetch-Mode", "navigate")
            .addHeader("Sec-Fetch-Site", "same-site").addHeader("Sec-Fetch-User", "?1")
            .addHeader("Upgrade-Insecure-Requests", "document").setClientHeader().build()
        val xfilesResponse = performRequest(xfilesRequest).get()
        val xfilesPage = xfilesResponse.getBody()
        val selectors = hashMapOf(
            "studentName" to "#lblDisplayName",
            // huge ass selector generated by firefox, probably volatile. Too bad!
            "studentId" to ".col-lg-9 > div:nth-child(1) > div:nth-child(2) > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2)",
//            "studentImage" to "div[style*=\"ProfileImage\"]",
            // get first term in academic history
            "term" to "#AcademicHistory > tbody > tr:nth-child(1) > td:nth-child(1)",
            "program" to "#lblCurrentProgram",
            "cgpa" to "#lblLatestCGPA",
            "sgpa" to "#lblCurrentGPA",
            "creditsCompleted" to "#lblEarnedCredits",
        )
        val document = Jsoup.parse(xfilesPage)
        val textValues = selectors.mapValues {
            document.select(it.value).text()
        }.toMutableMap()
        textValues["cgpa"] = textValues["cgpa"]!!.split("/")[0]
        textValues["sgpa"] = textValues["sgpa"]!!.split("/")[0]
        if (textValues.any {
                it.value.isEmpty()
            }) throw Exception("Couldn't extract x files info!")
        val imageUrl = extractBackgroundImageUrl(
            document.select("div[style*=\"ProfileImage\"]").attr("style")
        ).replace("'", "") // remove trailing '' from parse result

        val termTokens = textValues["term"].orEmpty().split(" ")
        val termYear = termTokens.last()
        val termID = when (termTokens[0]) {
            "Fall" -> 10
            "Spring" -> 20
            "Summer" -> 30
            else -> -1
        }
        if (termID == -1) throw Exception("Current term is undefined! Either selector is wrong or page has been updated.")

        val imageBytestream = performRequest(imageUrl).get().body!!.byteStream()
        val bitmap = BitmapFactory.decodeStream(imageBytestream)
        textValues["studentImage"] = bitmapToB64(bitmap)
        return XFilesInfo(
            studentName = textValues["studentName"].orEmpty(),
            studentID = textValues["studentId"].orEmpty(),
            studentImage = textValues["studentImage"].orEmpty(),
            term = "$termYear$termID",
            program = textValues["program"].orEmpty(),
            cgpa = textValues["cgpa"].orEmpty(),
            sgpa = textValues["sgpa"].orEmpty(),
            creditsCompleted = textValues["creditsCompleted"].orEmpty()
        )
    }

    fun getMoodleNotifications(): List<Notification> {
        val (_, sesskey, userid) = connectToMoodle()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody =
            "[{\"index\":0,\"methodname\":\"message_popup_get_popup_notifications\",\"args\":{\"limit\":20,\"offset\":0,\"useridto\":$userid}}]".toRequestBody(
                mediaType
            )
        val moodleRequest = Request.Builder()
            .url("https://moodle.bau.edu.lb/lib/ajax/service.php?sesskey=$sesskey&info=message_popup_get_popup_notifications")
            .post(requestBody).build()
        val response = this.performRequest(moodleRequest).get().getBody()
        val gson = GsonBuilder().create()
        val responseObject = (JSONArray(response)[0] as JSONObject)
        val responseJson = gson.fromJson(responseObject.toString(), ResponseClass::class.java)
        if (responseJson.error == true) {
            throw Exception("Couldn't fetch notifications data!")
        }
        val notificationsListPojo = responseJson.data!!.notifications!!
        return notificationsListPojo
    }

    fun getSchedule(): List<Course> {
        val schedulePage = login(BauService.SCHEDULE).getBody()
        val currentTerm = Jsoup.parse(schedulePage).select("option").last()!!.attr("value")
        performRequest("https://banservices.bau.edu.lb/StudentRegistrationSsb/ssb/registrationHistory/reset?term=$currentTerm")
        val finalJson =
            performRequest("https://banservices.bau.edu.lb/StudentRegistrationSsb/ssb/classRegistration/getMeetingInformationForRegistrations").get()
                .getBody()

        val coursesType = object : TypeToken<List<Course>>() {}.type
        val courses = Gson().fromJson<List<Course>>(finalJson, coursesType)
        return courses
    }

    private fun performRequest(request: Request): CompletableFuture<Response> {
        val responseFuture = CompletableFuture<Response>()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                responseFuture.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                responseFuture.complete(response)
            }
        })

        return responseFuture
    }

    private fun performRequest(
        url: String, method: HTTPMethod = HTTPMethod.GET, body: FormBody? = null
    ): CompletableFuture<Response> {
        // error handling is done by caller
        // just return response
        // keep in mind future needs to block to get result
        val responseFuture = CompletableFuture<Response>()
        val request: Request =
            Request.Builder().url(url).request(method, body).removeHeader("User-Agent").addHeader(
                "User-Agent", this.userAgent
            ) // remove and add header to override default OKHTTP header
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                responseFuture.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                responseFuture.complete(response)
            }
        })

        return responseFuture
    }

    private fun get(url: String): Response? {
        return performRequest(url).get()
    }

    private fun post(url: String, body: FormBody?): Response? {
        return performRequest(url, HTTPMethod.POST, body).get()
    }

    private fun Request.Builder.setClientHeader(): Request.Builder {
        return this.removeHeader("User-Agent").addHeader("User-Agent", userAgent)
    }

    private fun extractBackgroundImageUrl(styleString: String): String {
        val regex = "url\\(([\"']?([^\"')]+)[\"']?)\\)"
        val match = Regex(regex).find(styleString)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun httpClientBuilder(): OkHttpClient {
        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        val timeoutSeconds = 240L
        val logger = HttpLoggingInterceptor()
        logger.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder().cache(
            Cache(
                directory = context.cacheDir, maxSize = 10L * 1024L * 1024L
            )
        )

            .addInterceptor(logger).cookieJar(cookieJar).followRedirects(true)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS).build()


        return client
    }


    private fun Request.Builder.request(
        method: HTTPMethod = HTTPMethod.GET, body: FormBody? = null
    ): Request.Builder {
        return if (method == HTTPMethod.GET) {
            this
        } else {
            this.post(body!!)
        }
    }


    private fun Response?.getBody(): String {
        return this!!.body!!.string()
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
        val activity = getActivity(this.context)
            ?: throw Exception("Couldn't get Activity from ActivityContext ! It's probably an ApplicationContext instead.")
        val liveData = MutableLiveData<T>()
        if (getCachedJson(cacheKey) == "" || forceNetwork) {
            // only start loading screen on network request
            val loadingScreen = LoadingScreen(activity)
            loadingScreen.startLoadingDialog()
            try {
                thread(start = true) {
                    try {
                        val output: T = callback()
                        val serializedOutput = Gson().toJson(output)
                        liveData.postValue(output)
                        serializeToPreferencesIfNotFound(
                            preferences, cacheKey, serializedOutput, true
                        )
                        activity.runOnUiThread {
                            // the request is done, remove loading dialog
                            loadingScreen.dismissDialog()
                        }
                    } catch (
                        e: Exception
                    ) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "Oops! Please refresh current page.",
                                Toast.LENGTH_LONG
                            )
                                .show()

                            loadingScreen.dismissDialog()
                        }
                        logStackTrace("HTTP Tag", e)
                    }
                }
            } catch (e: ExecutionException) {
                logStackTrace("HTTP Tag", e)
            } catch (e: InterruptedException) {
                logStackTrace("HTTP Tag", e)
            }

        } else {
            // if we already have a response, just return from cache
            // THIS CLASS IS NOT RESPONSIBLE FOR REFRESHING DATA
            // MAKE SURE ANOTHER CLASS DOES
            thread(start = true) {
                // read shared preferences on different thread. Small I/O lag, but very noticeable for users so start on new thread.
                val cachedJson = getCachedJson(cacheKey)
                liveData.postValue(deserializer(cachedJson))
            }
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


//class FailedToScrapeException(s: String) : Exception() {
//
//}


