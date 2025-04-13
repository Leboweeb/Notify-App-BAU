package com.example.bettermoodle

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.islandparadise14.mintable.model.ScheduleDay
import com.islandparadise14.mintable.model.ScheduleEntity
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class ScheduleUtils(context: Context) {

    private var preferences: EncryptedSharedPreferences = getPrefs(context)

    fun populateEvents(courseList: List<Course>): ArrayList<ScheduleEntity> {
        val eventsArray = ArrayList<ScheduleEntity>()
        val colors = listOf(
            "#b1e6ff",
            "#ffafa0",
            "#ffdf9f",
            "#a6ff98",
            "#acb2cb",
            "#e1ffb3",
            "#c9ccff",
            "#75f3ff",
            "#f9c064",
            "#c3baab",
            "#d4a373",
            "#e9ff70"
        )
        val mapType = object : TypeToken<HashMap<String, String>>() {}.type
        val colorMap: HashMap<String, String> = if (preferences.contains("colorMap")) {
            Gson().fromJson(
                preferences.getString("colorMap", "")!!,
                mapType
            )
        } else {
            HashMap()
        }
        for ((num, course) in courseList.withIndex()) {
            if (!colorMap.containsKey(course.name)) {
                colorMap[course.name ?: ""] = colors.random()
            }
            for (meetingTime in course.meetingTimes) {
                val title =
                    "${course.name} (${course.courseCode} - ${course.crn})"
                val event =
                    ScheduleEntity(
                        num,
                        title,
                        course.getCourseLocation(),
                        meetingTime.getDay(),
                        toScheduleFormat(meetingTime.beginTime!!),
                        toScheduleFormat(meetingTime.endTime!!),
                        colorMap[course.name]!!,
                        "#000000"
                    )
                eventsArray.add(event)
            }
        }
        // serialize the entire schedule and color map into encrypted preferences when we're done
        // if we haven't already
        serializeToPreferencesIfNotFound(preferences, "schedule", Gson().toJson(eventsArray))
        serializeToPreferencesIfNotFound(preferences, "colorMap", Gson().toJson(colorMap))
        return eventsArray
    }


    @JvmOverloads
    fun refreshSchedule(refreshSchedule: Boolean = false, refreshColors: Boolean = false) {
        val refreshValue = { key: String, refresh: Boolean ->
            if (refresh)
                preferences.edit().remove(key).apply()
        }
        refreshValue("schedule", refreshSchedule)
        refreshValue("colorMap", refreshColors)
    }


}


private fun Course.getCourseLocation(): String {
    return this.meetingTimes[0].room ?: ""
}

fun toScheduleFormat(string: String): String {
    // this is because we want to convert the string "1401" to "14:01" for use in the ScheduleEntity object.
    return string.replaceRange(2, 2, ":")
}

fun toDate(string: String): Date {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.parse(string, ParsePosition(0))!!
}

fun to12Hour(string: String?): String {
    val time = LocalTime.parse(string ?: "", DateTimeFormatter.ofPattern("HHmm"))
    return time.format(DateTimeFormatter.ofPattern("hh:mma", Locale.getDefault()))
}

private fun MeetingTime.getDay(): Int {
    val days = mapOf(
        "monday" to this.monday!!,
        "tuesday" to this.tuesday!!,
        "wednesday" to this.wednesday!!,
        "thursday" to this.thursday!!,
        "friday" to this.friday!!,
        "saturday" to this.saturday!!,
        "sunday" to this.sunday!!,
    )

    return when (days.filter { it.value }.keys.last()) {
        "monday" -> ScheduleDay.MONDAY
        "tuesday" -> ScheduleDay.TUESDAY
        "wednesday" -> ScheduleDay.WEDNESDAY
        "thursday" -> ScheduleDay.THURSDAY
        "friday" -> ScheduleDay.FRIDAY
        "saturday" -> ScheduleDay.SATURDAY
        "sunday" -> ScheduleDay.SUNDAY
        else -> -1
    }
}
//
//
//
//fun scheduleToArrayList(context: Context): ArrayList<String>? {
//    val preferences = getPrefs(context)
//    val cachedScheduleString = preferences!!.getString("schedule", "")
//    if (cachedScheduleString.isNullOrBlank())
//        return null
//    val scheduleToArray = { json: String ->
//        val coursesList = ArrayList<String>()
//        val schedule = JSONObject(json)
//        val days = listOf(schedule["M"], schedule["T"], schedule["W"], schedule["R"])
//        val daysMap = hashMapOf(
//            Pair(0, "Monday"),
//            Pair(1, "Tuesday"),
//            Pair(2, "Wednesday"),
//            Pair(3, "Thursday"),
//        )
//        days.forEachIndexed { index, day ->
//            daysMap[index]?.let { coursesList.add(it) }
//            val courseNames = ArrayList<String>()
//            for (i in 0..<(day as JSONArray).length()) {
//                val obj = day[i] as JSONObject
//                courseNames.add("${obj["name"]}\n${obj["time"]} (${obj["location"]})")
//            }
//            coursesList.addAll(courseNames)
//        }
//
//        coursesList
//    }
//
//    return scheduleToArray(cachedScheduleString)
//
//}

