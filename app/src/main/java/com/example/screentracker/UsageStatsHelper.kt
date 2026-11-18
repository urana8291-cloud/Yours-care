package com.example.screentracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageInfo(
        val packageName: String,
            val appName: String,
                val icon: Drawable?,
                    val timeInForegroundMs: Long
)

object UsageStatsHelper {

        fun hasUsageStatsPermission(context: Context): Boolean {
                    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                            val mode = appOps.unsafeCheckOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                                                        Process.myUid(),
                                                                    context.packageName
                            )
                                    return mode == AppOpsManager.MODE_ALLOWED
        }

            fun getTodayAppUsage(context: Context): Pair<Long, List<AppUsageInfo>> {
                        val usageStatsManager =
                                    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                                            val pm = context.packageManager

                                                    val calendar = Calendar.getInstance().apply {
                                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                                                set(Calendar.MINUTE, 0)
                                                                                            set(Calendar.SECOND, 0)
                                                                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                            val startTime = calendar.timeInMillis
                                                                    val endTime = System.currentTimeMillis()

                                                                            val events = usageStatsManager.queryEvents(startTime, endTime)
                                                                                    val event = UsageEvents.Event()

                                                                                            val foregroundStartMap = mutableMapOf<String, Long>()
                                                                                                    val totalTimeMap = mutableMapOf<String, Long>()

                                                                                                            while (events.hasNextEvent()) {
                                                                                                                            events.getNextEvent(event)
                                                                                                                                        val pkg = event.packageName ?: continue

                                                                                                                                                    when (event.eventType) {
                                                                                                                                                                        UsageEvents.Event.ACTIVITY_RESUMED -> {
                                                                                                                                                                                                foregroundStartMap[pkg] = event.timeStamp
                                                                                                                                                                        }
                                                                                                                                                                                        UsageEvents.Event.ACTIVITY_PAUSED -> {
                                                                                                                                                                                                                val start = foregroundStartMap.remove(pkg)
                                                                                                                                                                                                                                    if (start != null && event.timeStamp > start) {
                                                                                                                                                                                                                                                                totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + (event.timeStamp - start)
                                                                                                                                                                                                                                    }
                                                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                            }

                                                                                                                    for ((pkg, start) in foregroundStartMap) {
                                                                                                                                    if (endTime > start) {
                                                                                                                                                        totalTimeMap[pkg] = (totalTimeMap[pkg] ?: 0L) + (endTime - start)
                                                                                                                                    }
                                                                                                                    }

                                                                                                                            val appList = mutableListOf<AppUsageInfo>()
                                                                                                                                    var totalScreenTime = 0L

                                                                                                                                            for ((pkg, duration) in totalTimeMap) {
                                                                                                                                                            if (duration < 60_000L) continue
                                                                                                                                                                        if (pkg == context.packageName) continue

                                                                                                                                                                                    try {
                                                                                                                                                                                                        val appInfo = pm.getApplicationInfo(pkg, 0)
                                                                                                                                                                                                                        if (pm.getLaunchIntentForPackage(pkg) == null) continue

                                                                                                                                                                                                                                        val appName = pm.getApplicationLabel(appInfo).toString()
                                                                                                                                                                                                                                                        val icon = pm.getApplicationIcon(appInfo)
                                                                                                                                                                                                                                                                        appList.add(AppUsageInfo(pkg, appName, icon, duration))
                                                                                                                                                                                                                                                                                        totalScreenTime += duration
                                                                                                                                                                                    } catch (_: PackageManager.NameNotFoundException) {
                                                                                                                                                                                    }
                                                                                                                                            }

                                                                                                                                                    appList.sortByDescending { it.timeInForegroundMs }
                                                                                                                                                            return Pair(totalScreenTime, appList)
            }

                fun formatDuration(millis: Long): String {
                            val hours = TimeUnit.MILLISECONDS.toHours(millis)
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
                                            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                }
}

                                                                                                                                                                                                                                    }
                                                                                                                                                                        }
                                                                                                                                                    }
                                                    }
        }
}
)