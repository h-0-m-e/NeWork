package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nework.adapter.EventViewHolder
import ru.netology.nework.databinding.EventFragmentBinding
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.ui.CreateFragment.Companion.textArg
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.EventViewModel

class EventFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        val eventViewModel: EventViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val currentEventId = requireArguments().textArg?.toLong()

        val eventInteractionListener = EventOnInteractionListener(
            this@EventFragment.requireActivity(),
            binding.root,
            eventViewModel
        )

        binding.publication.apply {
            eventViewModel.data.observe(viewLifecycleOwner) { it ->
                val viewHolder = EventViewHolder(binding.publication, eventInteractionListener)
                val event = it.events.find { it.id.toLong() == currentEventId }
                event?.let { viewHolder.bind(event) }
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}
