package com.pavlovalexey.pleinair.calendar.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pavlovalexey.pleinair.R
import com.pavlovalexey.pleinair.databinding.FragmentCalendarBinding
import com.pavlovalexey.pleinair.calendar.adapter.EventAdapter
import com.pavlovalexey.pleinair.calendar.model.Event
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    @Inject
    lateinit var adapter: EventAdapter
    @Inject
    lateinit var viewModel: CalendarViewModel

    private lateinit var binding: FragmentCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        val firestore = FirebaseFirestore.getInstance()
        val eventsCollection = firestore.collection("events")
        eventsCollection.get()
            .addOnSuccessListener { result ->
                val eventsList = mutableListOf<Event>()
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    eventsList.add(event)
                }
                adapter.submitList(eventsList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
        adapter = EventAdapter()
        binding.trackRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.trackRecyclerView.adapter = adapter

        // Наблюдение за событиями
        viewModel.events.observe(viewLifecycleOwner, Observer { events ->
            Log.d("CalendarFragment", "Events observed: $events")
            adapter.submitList(events)
        })

        // Наблюдение за ошибками
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })

        // Обработка нажатия кнопки создания нового события
        binding.fabCreateEvent.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                viewModel.checkUserEvent(userId) { canCreate ->
                    if (canCreate) {
//                        findNavController().navigate(R.id.action_calendarFragment_to_NewEventFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Вы уже создали одно событие!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Поиск события
        binding.searchEditText.addTextChangedListener { text ->
            viewModel.searchEvents(text.toString())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadEvents()
    }
}
