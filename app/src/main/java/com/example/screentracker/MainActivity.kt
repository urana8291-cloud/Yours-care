package com.example.screentracker

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.screentracker.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

        private lateinit var binding: ActivityMainBinding
            private lateinit var adapter: AppUsageAdapter
                private val dao by lazy { AppDatabase.getDatabase(this).statsDao() }

                    override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                        binding = ActivityMainBinding.inflate(layoutInflater)
                                                setContentView(binding.root)

                                                        setupRecyclerView()
                                                                setupClickListeners()
                                                                        observeTodayVideoCounts()
                    }

                        override fun onResume() {
                                    super.onResume()
                                            checkPermissionsState()
                                                    loadTodayScreenTime()
                        }

                            private fun setupRecyclerView() {
                                        adapter = AppUsageAdapter()
                                                binding.rvAppUsage.layoutManager = LinearLayoutManager(this)
                                                        binding.rvAppUsage.adapter = adapter
                            }

                                private fun setupClickListeners() {
                                            binding.btnUsagePerm.setOnClickListener {
                                                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                            }

                                                    binding.btnAccessPerm.setOnClickListener {
                                                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                                    }

                                                            binding.btnMonthlyReport.setOnClickListener {
                                                                            showMonthlyReportDialog()
                                                            }
                                }

                                    private fun checkPermissionsState() {
                                                val hasUsage = UsageStatsHelper.hasUsageStatsPermission(this)
                                                        val hasAccess = isAccessibilityServiceEnabled()

                                                                binding.btnUsagePerm.isEnabled = !hasUsage
                                                                        binding.btnUsagePerm.text = if (hasUsage) "1. Usage Granted ✓" else "1. Usage Access"

                                                                                binding.btnAccessPerm.isEnabled = !hasAccess
                                                                                        binding.btnAccessPerm.text = if (hasAccess) "2. Counter Active ✓" else "2. Video Counter"

                                                                                                binding.cardPermissions.visibility =
                                                                                                            if (hasUsage && hasAccess) View.GONE else View.VISIBLE
                                    }

                                        private fun isAccessibilityServiceEnabled(): Boolean {
                                                    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
                                                            val enabledServices =
                                                                        am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                                                                                return enabledServices.any {
                                                                                                it.resolveInfo.serviceInfo.packageName == packageName
                                                                                }
                                        }

                                            private fun observeTodayVideoCounts() {
                                                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                                                binding.tvDateHeader.text =
                                                                            SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())

                                                                                    lifecycleScope.launch {
                                                                                                    dao.insertIfNotExists(DailyStatEntity(date = today))
                                                                                                                dao.observeDailyStat(today).collectLatest { stat ->
                                                                                                                                binding.tvReelsCount.text = (stat?.reelsCount ?: 0).toString()
                                                                                                                                                binding.tvTiktokCount.text = (stat?.tiktokCount ?: 0).toString()
                                                                                                                                                            }
                                                                                    }
                                            }

                                                private fun loadTodayScreenTime() {
                                                            if (!UsageStatsHelper.hasUsageStatsPermission(this)) return

                                                                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                                                                            lifecycleScope.launch(Dispatchers.IO) {
                                                                                            val (totalTimeMs, appList) = UsageStatsHelper.getTodayAppUsage(this@MainActivity)
                                                                                                        dao.insertIfNotExists(DailyStatEntity(date = today))
                                                                                                                    dao.updateScreenTime(today, totalTimeMs)

                                                                                                                                withContext(Dispatchers.Main) {
                                                                                                                                                    binding.tvTotalScreenTime.text = UsageStatsHelper.formatDuration(totalTimeMs)
                                                                                                                                                                    adapter.updateData(appList)
                                                                                                                                }
                                                                            }
                                                }

                                                    private fun showMonthlyReportDialog() {
                                                                val currentMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                                                                        val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

                                                                                lifecycleScope.launch(Dispatchers.IO) {
                                                                                                val monthlyRecords = dao.getMonthlyStats(currentMonthKey)

                                                                                                            val totalReels = monthlyRecords.sumOf { it.reelsCount }
                                                                                                                        val totalTiktok = monthlyRecords.sumOf { it.tiktokCount }
                                                                                                                                    val totalScreenMs = monthlyRecords.sumOf { it.totalScreenTimeMs }
                                                                                                                                                val trackedDays = monthlyRecords.size.coerceAtLeast(1)
                                                                                                                                                            val avgScreenMs = totalScreenMs / trackedDays

                                                                                                                                                                        val reportText = buildString {
                                                                                                                                                                                            appendLine("📊 Monthly Digital Report ($monthTitle)")
                                                                                                                                                                                                            appendLine("----------------------------------------")
                                                                                                                                                                                                                            appendLine("• Days Tracked: ${monthlyRecords.size}")
                                                                                                                                                                                                                                            appendLine("• Total Screen Time: ${UsageStatsHelper.formatDuration(totalScreenMs)}")
                                                                                                                                                                                                                                                            appendLine("• Daily Avg Screen Time: ${UsageStatsHelper.formatDuration(avgScreenMs)}")
                                                                                                                                                                                                                                                                            appendLine("• Total Instagram Reels: $totalReels")
                                                                                                                                                                                                                                                                                            appendLine("• Total TikTok Videos: $totalTiktok")
                                                                                                                                                                                                                                                                                                            appendLine("----------------------------------------")
                                                                                                                                                                                                                                                                                                                            appendLine("Daily Breakdown:")
                                                                                                                                                                                                                                                                                                                                            monthlyRecords.forEach { day ->
                                                                                                                                                                                                                                                                                                                                                                appendLine(
                                                                                                                                                                                                                                                                                                                                                                                            "${day.date}: ${UsageStatsHelper.formatDuration(day.totalScreenTimeMs)} | " +
                                                                                                                                                                                                                                                                                                                                                                                                                            "Reels: ${day.reelsCount} | TikTok: ${day.tiktokCount}"
                                                                                                                                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                        }

                                                                                                                                                                                    withContext(Dispatchers.Main) {
                                                                                                                                                                                                        AlertDialog.Builder(this@MainActivity)
                                                                                                                                                                                                                            .setTitle("$monthTitle Report")
                                                                                                                                                                                                                                                .setMessage(reportText)
                                                                                                                                                                                                                                                                    .setPositiveButton("Share / Export") { _, _ ->
                                                                                                                                                                                                                                                                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                                                                                                                                                                                                                                                                            type = "text/plain"
                                                                                                                                                                                                                                                                                                                                                        putExtra(Intent.EXTRA_SUBJECT, "$monthTitle Digital Report")
                                                                                                                                                                                                                                                                                                                                                                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                    startActivity(Intent.createChooser(shareIntent, "Export Report via"))
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                        .setNegativeButton("Close", null)
                                                                                                                                                                                                                                                                                                            .show()
                                                                                                                                                                                    }
                                                                                }
                                                    }
}

                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                )}
                                                                                                                                }
                                            }
                    }
}