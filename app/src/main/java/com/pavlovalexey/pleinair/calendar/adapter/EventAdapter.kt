package com.pavlovalexey.pleinair.calendar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.UtilItemCalendarBinding
import com.pavlovalexey.pleinair.calendar.model.Event
import com.squareup.picasso.Picasso

class EventAdapter(
    private val context: Context,
    private val userProfileImageUrl: String? // Передаем URL изображения пользователя в адаптер
) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = UtilItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, context, userProfileImageUrl)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: UtilItemCalendarBinding,
        private val context: Context,
        private val userProfileImageUrl: String? // Получаем URL изображения пользователя в ViewHolder
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            // Определяем, какое изображение использовать
            val imageUrlToLoad = when {
                !userProfileImageUrl.isNullOrEmpty() -> userProfileImageUrl
                !event.profileImageUrl.isNullOrEmpty() -> event.profileImageUrl
                else -> null
            }

            // Загрузка изображения с использованием Picasso
            if (imageUrlToLoad != null) {
                Picasso.get()
                    .load(imageUrlToLoad)
                    .placeholder(R.drawable.account_circle_50dp) // Заглушка на время загрузки
                    .into(binding.artworkImageView)
            } else {
                // Если URL не найден, используем заглушку
                binding.artworkImageView.setImageResource(R.drawable.account_circle_50dp)
            }

            // Устанавливаем другие данные события
            binding.city.text = event.city
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
