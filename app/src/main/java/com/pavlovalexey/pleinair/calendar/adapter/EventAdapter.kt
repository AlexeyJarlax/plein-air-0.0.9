package com.pavlovalexey.pleinair.calendar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pavlovalexey.pleinair.databinding.UtilItemCalendarBinding
import com.pavlovalexey.pleinair.calendar.model.Event
import com.squareup.picasso.Picasso

class EventAdapter : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = UtilItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(private val binding: UtilItemCalendarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            Picasso.get().load(event.avatarUrl).into(binding.artworkImageView)
            binding.city.text = event.city
            binding.place.text = event.place
            binding.day.text = event.date
            binding.time.text = event.time
            binding.ditales.text = event.description
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
