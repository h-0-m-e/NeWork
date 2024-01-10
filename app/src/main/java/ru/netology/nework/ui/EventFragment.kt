package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.adapter.EventPagingViewHolder
import ru.netology.nework.adapter.EventViewHolder
import ru.netology.nework.adapter.PostViewHolder
import ru.netology.nework.adapter.SpeakersAdapter
import ru.netology.nework.databinding.EventFragmentBinding
import ru.netology.nework.dto.User
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.EventViewModel

class EventFragment : Fragment() {

    private val eventViewModel: EventViewModel by viewModels()



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

        val hideButtonIsChecked =
            MutableLiveData(false)


        val currentEventId = requireArguments().textArg?.toLong()

        val eventInteractionListener = EventOnInteractionListener(
            this@EventFragment.requireActivity(),
            binding.root,
            eventViewModel
        )

        binding.publication.apply {
            eventViewModel.getById(currentEventId ?: 0)
            eventViewModel.lastEvent.observe(viewLifecycleOwner){ it ->
                if (it.id != 0){
                    for(id in it.speakerIds){
                        eventViewModel.getSpeakersById(id)
                    }
                }
                val viewHolder = EventViewHolder(
                    true,
                    binding.publication,
                    eventInteractionListener)
                it?.let { viewHolder.bind(it) }
            }
        }
        val speakersAdapter = SpeakersAdapter(eventInteractionListener)
        binding.publication.speakersList.adapter = speakersAdapter

        eventViewModel.lastEventSpeakers.observe(viewLifecycleOwner){
                speakersAdapter.submitList(it)
        }

        hideButtonIsChecked.observe(viewLifecycleOwner) {
            if (it){
                binding.publication.speakersList.isVisible = true
                binding.publication.hideSpeakersList.text = getString(R.string.hide)
            } else {
                binding.publication.speakersList.isVisible = false
                binding.publication.hideSpeakersList.text = getString(R.string.show)
            }
        }

        binding.publication.hideSpeakersList.setOnClickListener {
            hideButtonIsChecked.value = !hideButtonIsChecked.value!!
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
