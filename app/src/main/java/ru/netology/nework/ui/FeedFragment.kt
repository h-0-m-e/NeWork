package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.databinding.FeedFragmentBinding
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.types.ErrorType
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel

class FeedFragment: Fragment() {

    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val eventViewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FeedFragmentBinding.inflate(
            inflater,
            container,
            false
        )

        eventViewModel.isPostsShowed.observe(viewLifecycleOwner){
            if(it){
                binding.postsButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.purple_light_background
                    )
                )
                binding.eventsButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.transparent
                    )
                )
                binding.eventsParent.visibility = View.GONE
                binding.emptyEvents.visibility = View.GONE
                binding.postsParent.visibility = View.VISIBLE
            } else {
                binding.postsButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.transparent
                    )
                )
                binding.eventsButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this.requireContext(),
                        R.color.purple_light_background
                    )
                )
                binding.eventsParent.visibility = View.VISIBLE
                binding.emptyPosts.visibility = View.GONE
                binding.postsParent.visibility = View.GONE
            }
        }

        val eventInteractionListener = EventOnInteractionListener(
            this@FeedFragment.requireActivity(),
            binding.root,
            eventViewModel
        )
        val postInteractionListener = PostOnInteractionListener(
            this@FeedFragment.requireActivity(),
            binding.root,
            postViewModel
        )

        val swipeRefreshEvent = binding.swipeRefreshEvent
        val swipeRefreshPost = binding.swipeRefreshPost

        val eventAdapter = EventAdapter(eventInteractionListener)
        val postAdapter = PostAdapter(postInteractionListener)


        binding.listEvent.adapter = eventAdapter
        binding.listEvent.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        eventViewModel.data.observe(viewLifecycleOwner) { data ->
            eventAdapter.submitList(data.events)
            binding.emptyEvents.isVisible = data.empty && binding.eventsParent.isVisible
        }

        binding.listPost.adapter = postAdapter
        binding.listPost.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        postViewModel.data.observe(viewLifecycleOwner) { data ->
            postAdapter.submitList(data.posts)
            binding.emptyPosts.isVisible = data.empty && binding.postsParent.isVisible
        }


        binding.eventsButton.setOnClickListener {
            eventViewModel.isPostsShowed.value = false
        }

        binding.postsButton.setOnClickListener {
            eventViewModel.isPostsShowed.value = true
        }

        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshEvent.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    if(binding.eventsParent.isVisible){
                        binding.emptyEvents.visibility = View.VISIBLE
                        binding.emptyEvents.text = state.errorMessage
                        Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { eventViewModel.loadEvents() }
                            .show()
                    }
                ErrorType.SAVE ->
                        Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { eventViewModel.save() }
                            .show()

                else -> Unit

            }
        }

        postViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshPost.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    if(binding.postsParent.isVisible) {
                        Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { postViewModel.loadPosts() }
                            .show()
                    }
                ErrorType.SAVE ->
                    Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry) { postViewModel.save() }
                        .show()
                else -> Unit

            }
        }

        swipeRefreshEvent.setOnRefreshListener {
            eventViewModel.refresh()
        }

        swipeRefreshPost.setOnRefreshListener {
            postViewModel.refresh()
        }


        return binding.root
    }
}
