package com.pavlovalexey.pleinair.event.ui.eventList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.pavlovalexey.pleinair.event.model.Event
import com.pavlovalexey.pleinair.event.ui.eventLocation.getAddressFromLatLng

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EventItem(event: Event, modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val newAdress: String = getAddressFromLatLng(context, event.latitude, event.longitude).toString()

    Box(modifier = Modifier
        .padding(16.dp)
        .background(Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))) {
        Row {
            GlideImage(
                model = event.profileImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = newAdress, style = MaterialTheme.typography.h6)
                Text(text = "Date: ${event.date}", style = MaterialTheme.typography.body2)
                Text(text = "Time: ${event.time}", style = MaterialTheme.typography.body2)
                Text(text = event.description, style = MaterialTheme.typography.body2)
            }
        }
    }   }