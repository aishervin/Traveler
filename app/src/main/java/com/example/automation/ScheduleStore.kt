package com.example.automation

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ScheduledTask(
    val id: String,
    val instruction: String,
    val localDateTime: String,
    val recurrence: String, // once, daily, weekly
    val createdAt: String
)

object ScheduleStore {
    private const val PREF_NAME = "baarbarg_schedule_prefs"
    private const val KEY_TASKS = "schedule.tasks"

    fun listTasks(context: Context): List<ScheduledTask> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TASKS, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<ScheduledTask>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createTask(context: Context, instruction: String, localDateTime: String, recurrence: String): ScheduledTask {
        val task = ScheduledTask(
            id = "task_${System.currentTimeMillis()}",
            instruction = instruction,
            localDateTime = localDateTime,
            recurrence = recurrence,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )
        val list = listTasks(context).toMutableList()
        list.add(task)
        saveTasks(context, list)
        return task
    }

    fun cancelTask(context: Context, taskId: String): Boolean {
        val list = listTasks(context).toMutableList()
        val removed = list.removeAll { it.id == taskId }
        if (removed) {
            saveTasks(context, list)
        }
        return removed
    }

    private fun saveTasks(context: Context, tasks: List<ScheduledTask>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(tasks)
        prefs.edit().putString(KEY_TASKS, json).apply()
    }
}
