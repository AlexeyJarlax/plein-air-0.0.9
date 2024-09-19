package com.pavlovalexey.pleinair.event.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.event.model.Event

class EventAdapter : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.util_item_calendar, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(event: Event) {
            // Привяжите данные события к элементам UI
            itemView.findViewById<TextView>(R.id.city).text = event.city
            itemView.findViewById<TextView>(R.id.day).text = event.date
            itemView.findViewById<TextView>(R.id.time).text = event.time
            itemView.findViewById<TextView>(R.id.ditales).text = event.description
            // Загрузите изображение профиля
            val imageView = itemView.findViewById<ImageView>(R.id.artwork_image_view)
            Glide.with(itemView.context)
                .load(event.profileImageUrl)
                .placeholder(R.drawable.account_circle_50dp)
                .into(imageView)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.timestamp == newItem.timestamp // Или другой уникальный идентификатор
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}