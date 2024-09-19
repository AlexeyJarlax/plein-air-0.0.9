package com.pavlovalexey.pleinair.event.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.pavlovalexey.pleinair.event.model.Event

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EventItem(event: Event, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(16.dp)) {
        GlideImage(
            model = event.profileImageUrl,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(event.city, style = MaterialTheme.typography.h6)
            Text(event.date, style = MaterialTheme.typography.body2)
            Text(event.time, style = MaterialTheme.typography.body2)
            Text(event.description, style = MaterialTheme.typography.body2)
        }
    }
}