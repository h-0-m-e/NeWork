package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.EventPagingAdapter
import ru.netology.nework.adapter.PostPagingAdapter
import ru.netology.nework.databinding.FeedFragmentBinding
import ru.netology.nework.listener.EventOnInteractionListener
import ru.netology.nework.listener.PostOnInteractionListener
import ru.netology.nework.types.ErrorType
import ru.netology.nework.utils.StringArg
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.SignInUpViewModel

class FeedFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val eventViewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private val signViewModel: SignInUpViewModel by viewModels(
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

        eventViewModel.isPostsShowed.observe(viewLifecycleOwner) {
            if (it) {
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

        val eventAdapter = EventPagingAdapter(eventInteractionListener)
        val postAdapter = PostPagingAdapter(postInteractionListener)


        binding.listEvent.adapter = eventAdapter
        binding.listEvent.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                eventViewModel.data.collectLatest {
                    eventAdapter.submitData(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                eventAdapter.loadStateFlow.collectLatest {
                    binding.eventProgressBar.isVisible =
                    it.refresh is LoadState.Loading
                            || it.prepend is LoadState.Loading
                            || it.append is LoadState.Loading
                }
            }
        }
//        eventViewModel.data.observe(viewLifecycleOwner) { data ->
//            eventAdapter.submitList(data.events)
//            binding.emptyEvents.isVisible = data.empty && binding.eventsParent.isVisible
//        }

        binding.listPost.adapter = postAdapter
        binding.listPost.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                postViewModel.data.collectLatest {
                    postAdapter.submitData(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                postAdapter.loadStateFlow.collectLatest {
                    binding.postProgressBar.isVisible =
                        it.refresh is LoadState.Loading
                            || it.prepend is LoadState.Loading
                            || it.append is LoadState.Loading
                }
            }
        }
//        postViewModel.data.observe(viewLifecycleOwner) { data ->
//            postAdapter.submitList(data.posts)
//            binding.emptyPosts.isVisible = data.empty && binding.postsParent.isVisible
//        }

        signViewModel.dataState.observe(viewLifecycleOwner){
            if (it.success){
                postAdapter.refresh()
                eventAdapter.refresh()
            }
        }

        binding.eventsButton.setOnClickListener {
            eventViewModel.isPostsShowed.value = false
        }

        binding.postsButton.setOnClickListener {
            eventViewModel.isPostsShowed.value = true
        }

        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            when (state.error) {
                ErrorType.LOADING ->
                    if (binding.eventsParent.isVisible) {
                        binding.emptyEvents.visibility = View.VISIBLE
                        binding.emptyEvents.text = state.errorMessage
                        Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { eventAdapter.refresh() }
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
            when (state.error) {
                ErrorType.LOADING ->
                    if (binding.postsParent.isVisible) {
                        Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { postAdapter.refresh() }
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
            eventAdapter.refresh()
            binding.swipeRefreshEvent.isRefreshing = false
            binding.listEvent.smoothScrollToPosition(0)
        }

        swipeRefreshPost.setOnRefreshListener {
            postAdapter.refresh()
            binding.swipeRefreshPost.isRefreshing = false
            binding.listPost.smoothScrollToPosition(0)
        }


        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}
