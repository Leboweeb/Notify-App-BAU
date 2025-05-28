package com.example.notify

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class BauService(val url: String, val finalUrl: HttpUrl) {
    ICONNECT(
        "https://mis.bau.edu.lb/web/v12/iconnectv12/cas/intermediate.aspx?TargetURL=https://mis.bau.edu.lb/web/v12/iconnectv12/base/profileV2.aspx",
        "https://mis.bau.edu.lb/web/v12/iconnectv12/base/profileV2.aspx".toHttpUrl()
    ),
    MOODLE(
        "https://moodle.bau.edu.lb", "https://moodle.bau.edu.lb/my/".toHttpUrl()
    ),
    SCHEDULE(
        "https://banservices.bau.edu.lb/StudentRegistrationSsb/ssb/registrationHistory/registrationHistory",
        "https://banservices.bau.edu.lb/StudentRegistrationSsb/ssb/registrationHistory/registrationHistory".toHttpUrl()
    )
}


enum class Services(val cacheKey: String) {
    NOTIFICATIONS("moodle/notifications"),
    XFILES("iconnect/xfiles"),
    SCHEDULE("iconnect/schedule")
}
data class CourseGrade (
    val course : String,
    val credits: Int,
    val grade : String,
)

data class XFilesInfo(
    val studentName: String,
    val studentID: String,
    val studentImage: String,
    val term: String,
    val program: String,
    val cgpa: String,
    val sgpa: String,
    val creditsCompleted: String,
    val remainingPaymentsLBP : Int,
    val remainingPaymentsUSD: Int,
    // eg. ( Course, Credits, Grade )
    val grades : List<CourseGrade>,
    val holds : List<String>,
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

    override fun toString(): String {
        return "$subject\n\n$fullmessage"
    }
}

data class Course(
    @SerializedName("courseTitle") var name: String? = null,
    @SerializedName("courseReferenceNumber") var crn: String? = null,
    @SerializedName("subjectCourse") var courseCode: String? = null,
    @SerializedName("meetingTimes") @Expose var meetingTimes: List<MeetingTime>
)

data class MeetingTime(

    @SerializedName("beginTime") var beginTime: String? = null,
    @SerializedName("building") var building: String? = null,
    @SerializedName("buildingDescription") var buildingDescription: String? = null,
    @SerializedName("campus") var campus: String? = null,
    @SerializedName("campusDescription") var campusDescription: String? = null,
    @SerializedName("courseReferenceNumber") var courseReferenceNumber: String? = null,
    @SerializedName("endDate") var endDate: String? = null,
    @SerializedName("endTime") var endTime: String? = null,
    @SerializedName("friday") var friday: Boolean? = null,
    @SerializedName("monday") var monday: Boolean? = null,
    @SerializedName("room") var room: String? = null,
    @SerializedName("saturday") var saturday: Boolean? = null,
    @SerializedName("startDate") var startDate: String? = null,
    @SerializedName("sunday") var sunday: Boolean? = null,
    @SerializedName("thursday") var thursday: Boolean? = null,
    @SerializedName("tuesday") var tuesday: Boolean? = null,
    @SerializedName("wednesday") var wednesday: Boolean? = null
)


