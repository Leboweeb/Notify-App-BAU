package com.example.bettermoodle


import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.example.JavaNetCookieJar
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


enum class BauService(val url: String, val finalUrl: HttpUrl) {
    ICONNECT(
        "https://iconnect.bau.edu.lb",
        "https://mis.bau.edu.lb/web/v12/iconnectv12/base/portalhome.aspx".toHttpUrl()
    ),
    MOODLE(
        "https://moodle.bau.edu.lb", "https://moodle.bau.edu.lb/my/".toHttpUrl()
    )
}


data class XFilesInfo(
    val studentName: String,
    val studentID: String,
    val studentImage: String,
    val program: String,
    val cgpa: String,
    val sgpa: String,
    val creditsCompleted: String
)

class Data {
    @SerializedName("notifications")
    @Expose
    var notifications: List<Notification>? = null
}


class ResponseClass {
    @SerializedName("error")
    @Expose
    var error: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null
}

class Notification {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("useridfrom")
    @Expose
    var useridfrom: Int? = null

    @SerializedName("useridto")
    @Expose
    var useridto: Int? = null

    @SerializedName("subject")
    @Expose
    var subject: String? = null

    @SerializedName("shortenedsubject")
    @Expose
    var shortenedsubject: String? = null

    @SerializedName("text")
    @Expose
    var text: String? = null

    @SerializedName("fullmessage")
    @Expose
    var fullmessage: String? = null

    @SerializedName("fullmessageformat")
    @Expose
    var fullmessageformat: Int? = null

    @SerializedName("fullmessagehtml")
    @Expose
    var fullmessagehtml: String? = null

    @SerializedName("smallmessage")
    @Expose
    var smallmessage: String? = null

    @SerializedName("contexturl")
    @Expose
    var contexturl: String? = null

    @SerializedName("contexturlname")
    @Expose
    var contexturlname: String? = null

    @SerializedName("timecreated")
    @Expose
    var timecreated: Int? = null

    @SerializedName("timecreatedpretty")
    @Expose
    var timecreatedpretty: String? = null

    @SerializedName("timeread")
    @Expose
    var timeread: Int? = null

    @SerializedName("read")
    @Expose
    var read: Boolean? = null

    @SerializedName("deleted")
    @Expose
    var deleted: Boolean? = null

    @SerializedName("iconurl")
    @Expose
    var iconurl: String? = null

    @SerializedName("component")
    @Expose
    var component: String? = null

    @SerializedName("eventtype")
    @Expose
    var eventtype: String? = null

    @SerializedName("customdata")
    @Expose
    var customdata: String? = null
}


enum class HTTPMethod {
    GET, POST;

}

class LocalApi(private val username: String, private val password: String) {

    private val userAgent = RandomUserAgent.getRandomUserAgent()
    private val authURL = "https://eis.bau.edu.lb/commonauth"
    private val client = httpClientBuilder()

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
        val loginRequest =
            Request.Builder().url(authURL).removeHeader("User-Agent")
                .addHeader("User-Agent", this.userAgent)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Connection", "keep-alive")
                .post(requestBody)
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

    fun getXFilesInfo(): XFilesInfo {
        connectToIconnect()
        val xfilesRequest = Request.Builder()
            .url("https://mis.bau.edu.lb/web/v12/iconnectv12/cas/intermediate.aspx?TargetURL=https://mis.bau.edu.lb/web/v12/iconnectv12/base/profileV2.aspx")
            .addHeader("Accept-Language", "en-US,en;q=0.7")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
            .addHeader("Priority", "u=0, i")
            .addHeader("Sec-Fetch-Dest", "document")
            .addHeader("Sec-Fetch-Mode", "navigate")
            .addHeader("Sec-Fetch-Site", "same-site")
            .addHeader("Sec-Fetch-User", "?1")
            .addHeader("Upgrade-Insecure-Requests", "document")
            .setClientHeader()
            .build()
        val xfilesResponse =
            performRequest(xfilesRequest).get()
        val xfilesPage = xfilesResponse.getBody()
        val selectors = hashMapOf(
            "studentName" to "#lblDisplayName",
            // huge ass selector generated by firefox, probably volatile. Too bad!
            "studentId" to ".col-lg-9 > div:nth-child(1) > div:nth-child(2) > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2)",
//            "studentImage" to "div[style*=\"ProfileImage\"]",
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
        textValues["studentImage"] =
            extractBackgroundImageUrl(document.select("div[style*=\"ProfileImage\"]").attr("style"))
        return XFilesInfo(
            studentName = textValues["studentName"].orEmpty(),
            studentID = textValues["studentId"].orEmpty(),
            studentImage = textValues["studentImage"].orEmpty(),
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
            Request.Builder().url(url).request(method, body).removeHeader("User-Agent")
                .addHeader(
                    "User-Agent",
                    this.userAgent
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
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val cookieJar = JavaNetCookieJar(cookieManager)
        val timeoutSeconds = 120L
        val client = OkHttpClient.Builder()
//        .cache(
//            Cache(
//                directory = context.cacheDir,
//                maxSize = 10L * 1024L * 1024L
//            )
//        )
//            .addInterceptor { chain ->
//                chain.request().newBuilder().removeHeader("User-Agent")
//                    .addHeader("User-Agent", this.userAgent)
//                    .addHeader("Accept-Language", "en-US,en;q=0.7")
//                    .build()
//                    .let(chain::proceed)}
            .cookieJar(cookieJar).followRedirects(true)
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

}



