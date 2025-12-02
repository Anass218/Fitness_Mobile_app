package com.example.fitness_kzo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitness_kzo.databinding.ItemActivityBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityAdapter(
    private val onDeleteClick: (Api.ActivityRecordForRF) -> Unit
) : ListAdapter<Api.ActivityRecordForRF, ActivityAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtActivity: TextView = view.findViewById(R.id.txtActivity)
        private val txtDetail: TextView = view.findViewById(R.id.txtDetail)
        private val txtCalories: TextView = view.findViewById(R.id.txtCalories)
        private val txtDate: TextView = view.findViewById(R.id.txtDate)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(record: Api.ActivityRecordForRF) {
            txtActivity.text = record.type.replaceFirstChar { it.uppercase() }
            txtDetail.text = "${record.duration_minutes} min • ${record.details}"
            txtCalories.text = "${record.calorie_burned_kcal} kcal"
            txtDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(record.recorded_at) ?: "")

            btnDelete.setOnClickListener { onDeleteClick(record) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Api.ActivityRecordForRF>() {
        override fun areItemsTheSame(
            oldItem: Api.ActivityRecordForRF,
            newItem: Api.ActivityRecordForRF
        ): Boolean = oldItem.id == newItem.id && oldItem.type == newItem.type

        override fun areContentsTheSame(
            oldItem: Api.ActivityRecordForRF,
            newItem: Api.ActivityRecordForRF
        ): Boolean = oldItem == newItem
    }
}