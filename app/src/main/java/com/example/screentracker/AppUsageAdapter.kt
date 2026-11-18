package com.example.screentracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.screentracker.databinding.ItemAppUsageBinding

class AppUsageAdapter(private var items: List<AppUsageInfo> = emptyList()) :
    RecyclerView.Adapter<AppUsageAdapter.ViewHolder>() {

            class ViewHolder(val binding: ItemAppUsageBinding) : RecyclerView.ViewHolder(binding.root)

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                            val binding = ItemAppUsageBinding.inflate(
                                            LayoutInflater.from(parent.context), parent, false
                            )
                                    return ViewHolder(binding)
                }

                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val item = items[position]
                                        holder.binding.tvAppName.text = item.appName
                                                holder.binding.ivAppIcon.setImageDrawable(item.icon)
                                                        holder.binding.tvAppTime.text = UsageStatsHelper.formatDuration(item.timeInForegroundMs)
                    }

                        override fun getItemCount() = items.size

                            fun updateData(newItems: List<AppUsageInfo>) {
                                        items = newItems
                                                notifyDataSetChanged()
                            }
    }
    
                            )
                }
    }