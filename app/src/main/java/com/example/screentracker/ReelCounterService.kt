package com.example.screentracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReelCounterService : AccessibilityService() {

        private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            private var lastCountTimestamp = 0L
                private var lastTrackedPackage = ""

                    companion object {
                                private const val INSTAGRAM_PKG = "com.instagram.android"
                                        private const val TIKTOK_GLOBAL_PKG = "com.zhiliaoapp.musically"
                                                private const val TIKTOK_ASIA_PKG = "com.ss.android.ugc.trill"
                                                        private const val SWIPE_COOLDOWN_MS = 1300L
                    }

                        override fun onAccessibilityEvent(event: AccessibilityEvent?) {
                                    if (event == null) return
                                            val pkg = event.packageName?.toString() ?: return

                                                    if (pkg != INSTAGRAM_PKG && pkg != TIKTOK_GLOBAL_PKG && pkg != TIKTOK_ASIA_PKG) {
                                                                    return
                                                    }

                                                            if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                                                                            val now = System.currentTimeMillis()

                                                                                        if (now - lastCountTimestamp >= SWIPE_COOLDOWN_MS || pkg != lastTrackedPackage) {
                                                                                                            lastCountTimestamp = now
                                                                                                                            lastTrackedPackage = pkg
                                                                                                                                            recordVideoView(pkg)
                                                                                        }
                                                            }
                        }

                            private fun recordVideoView(pkg: String) {
                                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                                val dao = AppDatabase.getDatabase(applicationContext).statsDao()

                                                        serviceScope.launch {
                                                                        dao.insertIfNotExists(DailyStatEntity(date = today))
                                                                                    when (pkg) {
                                                                                                        INSTAGRAM_PKG -> dao.incrementReels(today)
                                                                                                                        TIKTOK_GLOBAL_PKG, TIKTOK_ASIA_PKG -> dao.incrementTikTok(today)
                                                                                    }
                                                        }
                            }

                                override fun onInterrupt() {
                                }
}

                                                                                        }
                                                    }
                    }
}