package com.example.notify

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate

/**
 * Implementation of App Widget functionality.
 */
class ScheduleWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

@SuppressLint("DiscouragedApi")
internal fun populateScheduleWidget(context: Context, views: RemoteViews) {

    // assume user has already fetched schedule. I am NOT making a duplicate function just for this.
    val scheduleDataJson = getPrefs(context).getString(Services.SCHEDULE.cacheKey, "") ?: ""
    if (scheduleDataJson.isEmpty())
        return

    val today = getTargetedDay()
    val courseList = gsonSerialze<Course>(scheduleDataJson).filter { course ->
        course.meetingTimes.any {
            it.getDay() == today
        }
    }
    val meetingTimesPairs = mutableListOf<Pair<MeetingTime, Course>>()
    for (course in courseList) {
        for (meetingTime in course.meetingTimes) {
            if (meetingTime.getDay() == today)
                meetingTimesPairs.add(Pair(meetingTime, course))
        }
    }
    val meetingTimesMap = meetingTimesPairs.toMap()
    val meetingTimes = meetingTimesPairs.map { it.first }.sortedBy { meetingTime ->
        toDate(toScheduleFormat(meetingTime.beginTime ?: ""))
    }

    // sort courses by their meeting times
    for ((index, meetingTime) in meetingTimes.withIndex()) {
        val time = context.resources.getIdentifier(
            "time_label_$index",
            "id",
            context.packageName
        )
        val activityName = context.resources.getIdentifier(
            "activity_$index",
            "id",
            context.packageName
        )

        views.setTextViewText(
            time,
            "${to12Hour(meetingTime.beginTime)}-${to12Hour(meetingTime.endTime)}"
        )
        views.setTextViewText(activityName, meetingTimesMap[meetingTime]?.name)

    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.schedule_widget)
    // handle widget click
    val prefs = getPrefs(context)
    val intent: Intent
    if (prefs.contains("username") && prefs.contains("password")) {
        intent = Intent(context, OptionPage2::class.java)
        intent.putExtra("destination", Services.SCHEDULE.cacheKey)
    } else intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    views.setOnClickPendingIntent(
        R.id.schedule_grid, pendingIntent
    )
    // Instruct the widget manager to update the widget
    views.setTextViewText(R.id.schedule_title, "${getTargetedDay().capitalized()} Schedule")
    populateScheduleWidget(context, views)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getTargetedDay(): String {
    var today = LocalDate.now().dayOfWeek.name.lowercase()
    if (today == "sunday") {
        today = LocalDate.now().plusDays(1L).dayOfWeek.name.lowercase()
    } else if (today == "saturday") {
        today = LocalDate.now().plusDays(2L).dayOfWeek.name.lowercase()
    }
    return today
}

private fun MeetingTime.getDay(): String {
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
        "monday" -> "monday"
        "tuesday" -> "tuesday"
        "wednesday" -> "wednesday"
        "thursday" -> "thursday"
        "friday" -> "friday"
        "saturday" -> "saturday"
        "sunday" -> "sunday"
        else -> ""
    }
}
